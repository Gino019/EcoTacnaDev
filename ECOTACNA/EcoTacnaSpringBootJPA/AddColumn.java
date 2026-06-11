import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AddColumn {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
        String user = "postgres.fhdnwwqiraybpakspegx";
        String password = "EcoTacnaJPA22";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            String sql = "ALTER TABLE subscriptions ADD COLUMN scheduled_cancellation BOOLEAN DEFAULT FALSE";
            stmt.executeUpdate(sql);
            System.out.println("Column added successfully!");
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
