import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckTransportUnits {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
        String user = "postgres.fhdnwwqiraybpakspegx";
        String password = "EcoTacnaJPA22";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            String sql = "SELECT tu.id, tu.collector_company_id, c.ruc, c.business_name, tu.plate, tu.unit_type, tu.capacity_liters, tu.brand, tu.model, tu.status, tu.created_at, tu.updated_at FROM transport_units tu LEFT JOIN companies c ON c.id = tu.collector_company_id ORDER BY tu.collector_company_id, tu.created_at DESC;";
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("ID | COLLECTOR_COMPANY_ID | RUC | BUSINESS_NAME | PLATE | UNIT_TYPE | CAPACITY_LITERS | BRAND | MODEL | STATUS | CREATED_AT | UPDATED_AT");
            while (rs.next()) {
                System.out.printf("%s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s%n",
                    rs.getString("id"),
                    rs.getString("collector_company_id"),
                    rs.getString("ruc"),
                    rs.getString("business_name"),
                    rs.getString("plate"),
                    rs.getString("unit_type"),
                    rs.getString("capacity_liters"),
                    rs.getString("brand"),
                    rs.getString("model"),
                    rs.getString("status"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
