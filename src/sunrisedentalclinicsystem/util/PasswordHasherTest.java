package sunrisedentalclinicsystem.util;

/**
 *
 * @author yasit
 */
public class PasswordHasherTest {

    public static void main(String[] args) {
        String[] passwords = { "pass123", "pass123", "pass123", "pass123" };

        for (String password : passwords) {
            System.out.println("Password: " + password);
            System.out.println("Hash: " + PasswordHasher.hashPassword(password));
            System.out.println();
        }
    }
}
