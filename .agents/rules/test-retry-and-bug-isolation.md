---
trigger: always_on
---

# Quy Tắc Giới Hạn Thử Test & Khoanh Vùng Sửa Bug

1. **Tối đa 3 lần thử cho mỗi lần test:** Khi chạy một bài test hoặc lệnh kiểm chứng (unit test, Gradle build, kiểm thử emulator/ADB) bị lỗi, chỉ được phép thử lại hoặc điều chỉnh nhanh tối đa **3 lần**.
2. **Dừng lại ngay nếu sau 3 lần vẫn lỗi:** Không được chạy thử tiếp lần thứ 4 hoặc chuyển sang làm tính năng/bước tiếp theo khi lỗi vẫn chưa được giải quyết.
3. **Khoanh vùng rõ bug trước khi sửa tiếp:** Phải phân tích log, stack trace, file và dòng gây lỗi cụ thể để khoanh vùng đúng nguyên nhân gốc (root cause) — phân biệt rõ lỗi do code nguồn, test cũ, hay môi trường/công cụ.
4. **Sửa xong & test lại đạt mới được làm tiếp:** Chỉ khi đã sửa đúng nguyên nhân gốc và chạy lại bài test xác nhận **PASS 100%** (0 lỗi, 0 bỏ qua) mới được phép tiếp tục các bước kế tiếp.
