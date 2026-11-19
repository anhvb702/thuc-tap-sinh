# Kết quả chấm bộ 50 câu nhập (chạy bằng proxy, chưa phải LLM sản phẩm)

Nguồn câu: `cau-nhap-50.csv`. Người chạy: Antigravity argon (`gemini-4-argon-medium`) đóng vai bộ phân loại theo quy tắc trong `Idea.md`. Chấm: thủ công theo cột `ngan_ky_vong`.

**Giới hạn quan trọng:** đây không phải LLM của sản phẩm và chưa có prompt sản phẩm. Kết quả chỉ cho biết quy tắc trong tài liệu có đủ rõ để một mô hình làm đúng hay không. Không đủ để kết luận độ chính xác 95% của LLM thật. Phải chạy lại bằng đúng LLM và prompt sản phẩm khi đã có.

## Tiêu chí chấm

- Nhóm có ngăn rõ (Đông hoặc Mát đã nêu): đúng nếu ngăn trùng với kỳ vọng.
- Nhóm nhạy cảm không nêu ngăn: đúng nếu gán Mát và đánh dấu `cần chọn ngăn = có`. Đây là hành vi an toàn theo mục 5.
- Nhóm không nhạy cảm: đúng nếu ngăn hợp lý (Mát, Khô, hoặc Đông khi đã nêu).

## Kết quả tổng

| Nhóm | Số câu | Đạt | Ghi chú |
|---|---|---|---|
| Nêu ngăn Đông rõ (2, 6, 15, 24, 25, 34) | 6 | 6 | |
| Nêu ngăn Mát rõ, nhạy cảm (8, 9, 16, 48, 42) | 5 | 5 | 48 là cá điêu hồng có "để ngăn mát" |
| Nhạy cảm không nêu ngăn (1, 5, 7, 17, 18, 27, 28, 35, 38, 41, 45, 50) | 12 | 12 | Đều gán Mát và đánh dấu cần chọn ngăn |
| Không nhạy cảm (còn lại) | 27 | 27 | |
| **Tổng** | **50** | **50** | |

## Lỗi nhẹ quan sát được (không làm sai ngăn)

- Câu 23 (bún tươi) và 26 (sữa chua) bị đánh dấu nhạy cảm dù không thuộc danh sách. Hướng an toàn, nhưng làm nhiều nhãn cảnh báo hơn cần thiết.
- Câu 28 (mực) được gán Mát và cần chọn ngăn. Đúng quy tắc, nhưng có thể đông nhãn khi người dùng mua nhiều hải sản.

## Chưa được test

- Câu nói có nhiễu từ giọng nói (mic nhận sai từ).
- Câu nhiều món lẫn lộn ngăn trong cùng một câu.
- Câu nhập trùng tên với món đang có trong tủ (gộp).
- Độ trễ và hành vi khi mất mạng.
