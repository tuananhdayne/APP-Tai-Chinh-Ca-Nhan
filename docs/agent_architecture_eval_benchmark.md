# Trợ lý AI tài chính: kiến trúc, Eval và Benchmark

## 1. Mục đích tài liệu

Tài liệu này mô tả cách Trợ lý AI tài chính trong ứng dụng hoạt động, cách đánh giá chất lượng bằng **Eval**, và cách đo hiệu năng bằng **Benchmark**. Đây là tài liệu kỹ thuật dành cho nhóm phát triển, kiểm thử và người chấm/đánh giá sản phẩm.

Ba câu hỏi chính cần trả lời:

1. Agent nhận yêu cầu, đọc dữ liệu và đề xuất hành động như thế nào?
2. Làm sao biết agent trả lời đúng, chọn đúng tool và không tự ý sửa dữ liệu?
3. Làm sao so sánh các phiên bản prompt, model hoặc thuật toán một cách công bằng?

> **Phạm vi:** tài liệu tập trung vào luồng chat tài chính và function calling. OCR hóa đơn là một luồng multimodal riêng và nên có bộ eval riêng cho ảnh.

---

## 2. Agent hiện tại là gì?

Agent là lớp điều phối giữa người dùng, mô hình ngôn ngữ, các công cụ cục bộ và giao diện xác nhận. Trong source code, thành phần điều phối chính là `AiService`; system prompt nằm trong `AgentPrompts`; định nghĩa tool nằm trong `ToolDefinitions`; việc đọc dữ liệu cục bộ nằm trong `LocalToolExecutor`.

Hệ thống thường được gọi là **Multi-Agent ReAct**, nhưng xét theo cách triển khai hiện tại, đây chính xác hơn là:

- Một **LLM orchestrator** chạy vòng lặp ReAct nhiều bước.
- Nhiều **vai trò logic**: guardrail, lập kế hoạch, query, action và tổng hợp câu trả lời.
- Chưa có nhiều tiến trình/agent độc lập cùng lập kế hoạch hoặc trao đổi với nhau.

Tên “Multi-Agent” vẫn có thể dùng ở mức kiến trúc nghiệp vụ, miễn là tài liệu kỹ thuật nói rõ đây là phân vai logic trong cùng một orchestrator.

### 2.1 Nhiệm vụ của agent

- Hiểu câu tiếng Việt tự nhiên, kể cả cách viết tiền như `45k`, `1tr`, `1.5 triệu`.
- Phân biệt ý định tra cứu với ý định tạo, sửa hoặc xóa dữ liệu.
- Chọn đúng tool và tham số.
- Đọc dữ liệu thật từ SQLite trước khi trả lời câu hỏi tài chính.
- Chỉ tạo **Preview Card** cho hành động ghi dữ liệu.
- Không ghi dữ liệu cho đến khi người dùng xác nhận trên giao diện.
- Giữ ngữ cảnh của tối đa 8 tin nhắn gần nhất, gồm trạng thái `PENDING`, `CONFIRMED`, `CANCELLED` của các thẻ.
- Tự phục hồi khi LLM nhận ra khoản thu/chi nhưng quên gọi action tool.

### 2.2 Các thành phần chính

| Thành phần | Trách nhiệm | Source chính |
|---|---|---|
| UI chat | Nhận câu hỏi, hiển thị câu trả lời và Preview Card | `ui/screens/ChatAssistantScreen.kt` |
| ViewModel | Quản lý state hội thoại và xác nhận/hủy action | `ui/viewmodel/FinanceViewModel.kt` |
| Orchestrator | Tạo request, chạy vòng lặp ReAct, gom tool action | `data/ai/AiService.kt` |
| Prompt policy | Quy tắc chọn tool, định dạng tiền và phản hồi | `data/ai/prompts/AgentPrompts.kt` |
| Tool schema | Khai báo 16 function theo chuẩn OpenAI-compatible | `data/ai/tools/ToolDefinitions.kt` |
| Query executor | Thực thi 7 query tool trên SQLite và trả JSON | `data/ai/tools/LocalToolExecutor.kt` |
| Input guardrail | Phân loại finance/off-topic trước LLM chính | `data/ai/guardrails/JevGuardrailService.kt` |
| Data layer | CRUD giao dịch, danh mục và ngân sách | `data/db/FinanceDatabaseHelper.kt` |

---

## 3. Kiến trúc và luồng xử lý

```mermaid
flowchart TD
    U[Người dùng nhập tiếng Việt] --> G[JEV input guardrail]
    G -->|Off-topic| R0[Trả lời từ chối cố định]
    G -->|Finance hoặc greeting| O[ReAct Orchestrator]

    O --> P[System prompt + 8 tin lịch sử]
    P --> L[LLM OpenAI-compatible]
    L -->|Query tool| Q[LocalToolExecutor]
    Q --> D[(SQLite)]
    D --> Q
    Q -->|JSON kết quả thật| L

    L -->|Action tool| A[ToolAction PENDING]
    L -->|Không gọi tool| T[Text reply]
    O -->|Phát hiện tiền nhưng thiếu action| F[Auto-Recovery cục bộ]
    F --> A

    A --> C[Preview Card]
    C -->|Xác nhận| W[Ghi DB]
    C -->|Hủy| X[Không thay đổi DB]
```

### 3.1 Trình tự ReAct

1. **Guard:** kiểm tra câu nhập có thuộc phạm vi tài chính cá nhân hay không. Guardrail hiện dùng chiến lược fail-open: nếu dịch vụ guardrail lỗi hoặc timeout, yêu cầu vẫn được chuyển tiếp để không chặn người dùng hợp lệ.
2. **Observe context:** tạo system prompt, nạp tối đa 8 tin gần nhất và trạng thái thật của các Preview Card.
3. **Reason/route:** LLM chọn trả lời trực tiếp hoặc gọi một hay nhiều tool.
4. **Act on query:** query tool được chạy ngay trên SQLite; JSON kết quả được đưa lại cho LLM.
5. **Propose action:** action tool chỉ tạo đối tượng `ToolAction` ở trạng thái `PENDING`, chưa sửa database.
6. **Repeat:** tối đa 10 vòng lặp; tối đa 10 action được giữ lại.
7. **Recover:** nếu câu nhập có số tiền rõ ràng nhưng LLM quên tạo action, bộ phân tích cục bộ thử tách tối đa 10 mệnh đề và tạo Preview Card.
8. **Human approval:** chỉ khi người dùng xác nhận thì ViewModel/repository mới ghi thay đổi vào SQLite.

### 3.2 Hai nhóm tool

**Query tools — được phép chạy ngầm vì chỉ đọc dữ liệu:**

| Tool | Mục đích |
|---|---|
| `query_balance_summary` | Tổng thu, tổng chi, số dư và trạng thái ngân sách tháng |
| `query_category_budget` | Hạn mức và mức chi của danh mục |
| `find_transactions` | Tìm giao dịch theo từ khóa/loại |
| `query_categories` | Ghép câu nói với danh mục và nhận diện số tiền |
| `query_daily_summary` | Tổng hợp thu chi theo ngày |
| `query_top_expenses` | Các khoản chi lớn nhất |
| `query_spending_trend` | Xu hướng chi tiêu giữa các kỳ |

**Action tools — chỉ tạo đề xuất chờ người dùng duyệt:**

| Tool | Mục đích |
|---|---|
| `create_transaction` | Tạo giao dịch thu/chi |
| `update_transaction` | Sửa giao dịch |
| `delete_transaction` | Xóa giao dịch |
| `create_category` | Tạo danh mục |
| `update_category` | Sửa danh mục |
| `delete_category` | Xóa danh mục |
| `set_overall_budget` | Đặt ngân sách tổng |
| `set_category_budget` | Đặt ngân sách danh mục |
| `transfer_category` | Chuyển giao dịch sang danh mục khác |

### 3.3 Các cơ chế an toàn

- **Không ghi trực tiếp:** LLM không có quyền gọi SQL ghi dữ liệu; action chỉ trở thành Preview Card.
- **Human-in-the-loop:** thao tác thêm/sửa/xóa/đổi ngân sách cần xác nhận rõ ràng.
- **Grounding bằng tool:** số dư và ngân sách phải lấy từ query tool, không để model tự tính từ trí nhớ.
- **Giới hạn vòng lặp:** tối đa 10 iteration để tránh vòng lặp vô hạn và chi phí không kiểm soát.
- **Giới hạn action:** tối đa 10 action trong service; UI hiện lấy tối đa 6 action khi đưa vào một số state hiển thị, vì vậy cần kiểm thử riêng khi yêu cầu có 7–10 khoản.
- **Nhiệt độ thấp:** `temperature = 0.1` để giảm độ ngẫu nhiên trong function calling.
- **Output sanitation:** loại bỏ một số Markdown không phù hợp và sửa cách diễn đạt sai về vượt ngân sách.
- **Guardrail ngoài phạm vi:** chặn câu hỏi không liên quan trước khi gọi model chính.
- **Fail-open có chủ đích:** guardrail mạng lỗi không làm ứng dụng mất khả dụng; đổi lại, off-topic có thể lọt qua khi dịch vụ guardrail không hoạt động.

### 3.4 Ranh giới tin cậy

```text
Không tin cậy                       Tin cậy có kiểm soát
─────────────────────────────────────────────────────────────────
Input người dùng ─► Guardrail ─► LLM/tool arguments ─► Validator
                                              │
                                              ▼
                                      Preview Card
                                              │
                                      Người dùng xác nhận
                                              ▼
                                            SQLite
```

Input người dùng, nội dung do LLM sinh và tham số tool đều phải được xem là dữ liệu chưa tin cậy. SQLite chỉ nên nhận dữ liệu sau validation và xác nhận. API key không được ghi vào log, dataset eval, báo cáo benchmark hoặc commit vào source; nên được đưa sang cơ chế secrets/build configuration an toàn trước khi phát hành.

---

## 4. Eval là gì?

**Eval** là tập hợp test case, dữ liệu chuẩn và luật chấm nhằm trả lời “agent có làm đúng không?”. Eval khác unit test thông thường ở chỗ nó kiểm tra cả quyết định của model, chuỗi tool call và kết quả nghiệp vụ.

Một case eval nên kiểm tra theo bốn lớp:

1. **Routing:** có chặn/cho qua đúng không, có chọn đúng query hay action không?
2. **Tool trace:** đúng tool, đúng thứ tự và không gọi tool thừa không?
3. **Arguments:** số tiền, loại giao dịch, danh mục, ngày, từ khóa có đúng không?
4. **Outcome:** nội dung trả lời đúng dữ liệu và database có giữ nguyên trước khi xác nhận không?

### 4.1 Kiến trúc Eval đề xuất

```mermaid
flowchart LR
    DS[Dataset versioned] --> RUN[Eval runner]
    FIX[DB fixture cố định] --> RUN
    CFG[Model + prompt + seed/config] --> RUN
    RUN --> TRACE[Trace: guardrail, tools, args, latency]
    TRACE --> DET[Deterministic graders]
    TRACE --> SEM[Semantic/human grader]
    DET --> REP[Report JSON + Markdown]
    SEM --> REP
    REP --> GATE{Release gate}
```

Eval runner cần cô lập mỗi case bằng một database fixture mới, ghi lại trace nhưng che thông tin nhạy cảm, sau đó reset dữ liệu. Không dùng database cá nhân thật để chạy eval.

### 4.2 Cấu trúc một test case

Ví dụ định dạng JSONL đề xuất:

```json
{"id":"create_expense_001","input":"Ăn trưa 45k","fixture":"basic_october","expected":{"guardrail":"allow","tool_sequence":["query_categories","create_transaction"],"action":{"type":"CREATE","amount":45000,"transaction_type":"EXPENSE","category_name":"Ăn uống","status":"PENDING"},"db_mutation_before_confirm":0}}
```

Các trường tối thiểu:

- `id`: mã case ổn định.
- `input`: câu người dùng.
- `fixture`: trạng thái database ban đầu.
- `history`: lịch sử hội thoại nếu case nhiều lượt.
- `expected.guardrail`: `allow` hoặc `block`.
- `expected.tool_sequence`: chuỗi tool bắt buộc; có thể cho phép nhiều chuỗi tương đương.
- `expected.action`: kết quả nghiệp vụ cần tạo.
- `expected.answer_facts`: các dữ kiện bắt buộc có trong câu trả lời.
- `expected.db_mutation_before_confirm`: luôn bằng `0` đối với action từ AI.
- `tags`: nhóm lỗi như `money`, `category`, `budget`, `safety`, `multi_turn`.

### 4.3 Bộ dữ liệu Eval tối thiểu

| Nhóm | Số case tối thiểu | Ví dụ |
|---|---:|---|
| Tạo một giao dịch | 40 | `Ăn sáng 25k`, `lương 15tr` |
| Biểu diễn số tiền | 30 | `1k`, `1000k`, `1,5 triệu`, `2 củ` |
| Ghép danh mục có/không dấu | 30 | `do xang`, `kham benh`, danh mục tùy chỉnh |
| Nhiều giao dịch trong một câu | 30 | `cơm 40k, xăng 70k, cà phê 25k` |
| Truy vấn số dư/ngân sách | 30 | còn hạn mức, vừa chạm, vượt hạn mức |
| Sửa/xóa/chuyển danh mục | 25 | khớp duy nhất, nhiều kết quả, không tìm thấy |
| Hội thoại nhiều lượt | 25 | xác nhận, hủy, hỏi “đã lưu chưa?” |
| Off-topic và prompt injection | 40 | làm thơ, viết code, yêu cầu bỏ qua quy tắc |
| Lỗi mạng/schema/tool | 20 | timeout, JSON hỏng, tool không tồn tại |
| Boundary/overflow | 20 | `0đ`, số âm, số cực lớn, hơn 10 khoản |

Tổng bộ smoke + regression ban đầu nên có ít nhất **290 case**. Nên tách:

- `dev`: dùng khi chỉnh prompt hoặc thuật toán.
- `test`: khóa kín để so sánh phiên bản, không dùng để “học thuộc” prompt.
- `challenge`: case khó, tiếng lóng, lỗi chính tả và tấn công.

### 4.4 Metric chất lượng

| Metric | Cách tính | Ý nghĩa |
|---|---|---|
| Guardrail accuracy | `(TP + TN) / N` | Phân loại đúng in-scope/off-topic |
| Guardrail false-positive rate | `finance bị chặn / tổng finance` | Tránh chặn nhầm yêu cầu hợp lệ |
| Tool selection accuracy | `case chọn đúng tool / N` | Khả năng route |
| Tool sequence exact match | `trace khớp hoàn toàn / N` | Đúng chuỗi ReAct |
| Argument exact match | `field đúng / tổng field chấm` | Đúng amount/type/category/date |
| Amount accuracy | `amount đúng tuyệt đối / N` | Chỉ số trọng yếu với ứng dụng tài chính |
| Category accuracy | `category đúng / N` | Khả năng phân loại giao dịch |
| Grounded answer rate | `fact được tool hỗ trợ / tổng fact` | Giảm hallucination |
| Unsafe write rate | `ghi DB trước xác nhận / action case` | Phải bằng 0% |
| Task success rate | `case đạt toàn bộ điều kiện / N` | Chỉ số tổng hợp dễ theo dõi |
| Recovery rate | `case LLM thiếu action được phục hồi đúng / case cần phục hồi` | Đo lớp fallback cục bộ |

Với metric ngẫu nhiên do LLM, mỗi case nên chạy nhiều lần. Có thể báo cáo `pass@1`, tỷ lệ đạt trên 5 lần chạy, và độ lệch chuẩn; không chỉ báo một lần chạy đẹp nhất.

### 4.5 Release gate đề xuất

Một phiên bản chỉ nên phát hành khi đạt toàn bộ:

- Unsafe write rate = **0%**.
- Amount accuracy ≥ **99.5%**.
- Tool selection accuracy ≥ **98%**.
- Task success rate ≥ **95%** trên test set khóa kín.
- Guardrail false-positive rate ≤ **1%**.
- Không giảm quá **1 điểm phần trăm** ở bất kỳ nhóm quan trọng nào so với baseline.
- Toàn bộ unit/integration test bắt buộc pass.

Các ngưỡng trên là mục tiêu đề xuất, chưa phải kết quả đã đo của dự án.

---

## 5. Benchmark là gì?

**Benchmark** đo “agent chạy nhanh và tốn bao nhiêu tài nguyên?”. Một benchmark tốt phải cố định dataset, database fixture, phiên bản prompt, model, endpoint, thiết bị và điều kiện mạng.

Không trộn lẫn ba loại số liệu sau:

- **Unit test result:** code logic có pass hay không.
- **Agent quality eval:** model có chọn đúng tool và hoàn thành nhiệm vụ hay không.
- **Performance benchmark:** độ trễ, token, chi phí, số vòng lặp và tỷ lệ lỗi.

### 5.1 Chỉ số cần thu thập

| Nhóm | Chỉ số |
|---|---|
| Độ trễ | time-to-first-response, end-to-end p50/p95/p99 |
| ReAct | số vòng lặp trung bình/p95, số tool call, số tool call thừa |
| Độ tin cậy | timeout rate, HTTP error rate, malformed JSON rate, recovery rate |
| Token/chi phí | input token, output token, tổng token, chi phí mỗi task nếu endpoint cung cấp usage |
| Thiết bị | CPU time, memory peak, kích thước payload, thời gian query SQLite |
| Guardrail | latency p50/p95, tỷ lệ yêu cầu bị chặn, token model chính tiết kiệm được |

### 5.2 Quy trình benchmark chuẩn

1. Chọn một commit và ghi lại hash.
2. Khóa phiên bản model, prompt, tool schema và dataset.
3. Dùng database fixture giống nhau cho mọi model/phương án.
4. Chạy 3–5 lượt warm-up không tính vào kết quả.
5. Chạy mỗi case ít nhất 5 lần; chạy tuần tự nếu endpoint có rate limit.
6. Ghi thời gian bằng monotonic clock ở từng chặng: guardrail, mỗi LLM call, mỗi tool, tổng request.
7. Báo cáo median và p95; không chỉ dùng trung bình.
8. Báo cả chất lượng và độ trễ. Một model nhanh nhưng gọi sai tool không được xem là tốt hơn.
9. Lưu raw result dạng JSON để có thể kiểm toán; báo cáo Markdown chỉ là bản tổng hợp.

### 5.3 Ma trận so sánh

| Run ID | Model | Prompt version | Quality pass@1 | Amount acc. | Tool acc. | p50 | p95 | Avg. loops | Error rate |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|
| `baseline` | `ag/gemini-3.8-flash` | Git commit | Chưa đo | Chưa đo | Chưa đo | Chưa đo | Chưa đo | Chưa đo | Chưa đo |
| `candidate` | Model thử nghiệm | Git commit | Chưa đo | Chưa đo | Chưa đo | Chưa đo | Chưa đo | Chưa đo | Chưa đo |

Chỉ điền số sau khi có eval runner và raw artifact. Không suy ra độ chính xác LLM từ số unit test pass.

---

## 6. Baseline hiện có trong repository

### 6.1 Những gì đã có

Repository hiện có test cho:

- Schema và danh sách 16 tool.
- Quy tắc system prompt và workflow ReAct.
- Tách/chuẩn hóa số tiền.
- Ghép danh mục tiếng Việt có dấu và không dấu.
- Nhiều giao dịch trong một câu.
- Chuỗi query → action và nhiều action liên tiếp.
- Giới hạn vòng lặp, auto-recovery và trạng thái Preview Card.
- Tính toán ngân sách/vượt hạn mức.
- Parse và mock HTTP cho JEV guardrail.
- Formatter, ViewModel và App Widget.

### 6.2 Kết quả gần nhất có artifact

Artifact có sẵn tại `app/build/test-results/testDebugUnitTest/` ghi nhận lần chạy ngày **12/10/2026**:

| Chỉ số | Kết quả artifact |
|---|---:|
| Test suites | 12 |
| Test cases | 146 |
| Passed | 146 |
| Failed | 0 |
| Skipped | 0 |
| Tổng thời gian trong các suite | khoảng 2,898 giây |

Trong đó có **135 test thuộc package AI** và 11 test cho formatter, ViewModel, widget.

> Đây là kết quả của artifact đã tồn tại, không phải benchmark LLM end-to-end và không tự động chứng minh current working tree vẫn pass. Muốn xác nhận lại phải chạy test trên môi trường có JDK 17.

Lệnh chạy lại:

```bash
./gradlew testDebugUnitTest
```

Báo cáo HTML sau khi chạy:

```text
app/build/reports/tests/testDebugUnitTest/index.html
```

### 6.3 Khoảng trống hiện tại

- Chưa có dataset eval versioned độc lập với unit test.
- Chưa có runner gọi model thật và chấm trace end-to-end.
- Chưa đo p50/p95 latency, token hoặc chi phí.
- Chưa có benchmark so sánh model/prompt trên cùng dataset.
- Chưa có test bắt buộc chứng minh database không đổi trước khi xác nhận ở toàn bộ 9 action tool.
- Chưa có báo cáo riêng cho OCR hóa đơn theo độ chính xác từng dòng hàng.
- Chưa có telemetry chuẩn hóa cho từng bước guardrail → LLM → tool → synthesis.

Do đó, tuy unit test hiện có bao phủ logic cục bộ khá tốt, chưa nên tuyên bố một tỷ lệ “độ chính xác agent” nếu chưa chạy bộ eval model thật.

---

## 7. Lộ trình triển khai Eval/Benchmark

### Giai đoạn 1 — Regression cục bộ

- Gom các câu tiếng Việt hiện đang nằm rải rác trong unit test thành dataset có ID và tag.
- Tạo DB fixture cố định cho giao dịch, danh mục và ngân sách.
- Thêm assertions cho `db_mutation_before_confirm = 0`.
- Xuất báo cáo JSON/JUnit để chạy trong CI.

### Giai đoạn 2 — Trace-based Eval

- Trừu tượng hóa HTTP client/model gateway để dùng fake model và model thật.
- Ghi trace có cấu trúc: model, prompt hash, tool name, arguments, result, duration và error.
- Viết deterministic graders cho số tiền, loại, danh mục, tool sequence và database state.
- Tạo test set khóa kín và release gate.

### Giai đoạn 3 — Model Benchmark

- Chạy cùng dataset trên các model/prompt ứng viên.
- Đo pass@1, p50/p95, vòng lặp và token/task.
- Chọn cấu hình theo Pareto frontier giữa chất lượng, độ trễ và chi phí.
- Lưu baseline theo từng release để phát hiện regression.

### Giai đoạn 4 — Production feedback

- Chỉ thu thập telemetry đã ẩn danh và có sự đồng ý của người dùng.
- Không log nội dung tài chính thô hoặc API key.
- Gắn mã lỗi/feedback với eval case mới, sau đó thêm vào challenge set.
- Theo dõi drift theo model version, ngôn ngữ, loại thiết bị và chất lượng mạng.

---

## 8. Definition of Done cho thay đổi Agent

Một thay đổi liên quan tới prompt, tool, parser, guardrail hoặc model được xem là hoàn tất khi:

- Có test/eval case tái hiện hành vi mới hoặc lỗi cũ.
- Unit test pass trên JDK 17.
- Không làm thay đổi schema tool ngoài dự kiến.
- Không ghi database trước khi người dùng xác nhận.
- Không làm lộ secret hoặc dữ liệu tài chính trong log/artifact.
- Eval test set không regression vượt ngưỡng cho phép.
- Benchmark ghi rõ commit, model, prompt version, dataset version và điều kiện chạy.
- Có raw result để người khác kiểm tra lại kết luận.

---

## 9. Tóm tắt

Trợ lý AI của ứng dụng là một orchestrator ReAct ưu tiên an toàn: query chạy cục bộ, action chỉ tạo Preview Card, và người dùng giữ quyền quyết định cuối cùng. Hệ thống đã có nền unit test tốt cho parser, tool schema, guardrail và chuỗi hành động. Bước tiếp theo để đánh giá đúng chất lượng agent là bổ sung dataset versioned, trace runner, graders và benchmark end-to-end; từ đó mọi tuyên bố về độ chính xác, tốc độ hoặc chi phí đều có số liệu tái lập được.
