

public class Users {
    public String account;
    public String password;

    public Users() {
        // Bắt buộc để Firebase sử dụng
    }

    public Users(String account, String password) {
        this.account = account;
        this.password = password;
    }
}
