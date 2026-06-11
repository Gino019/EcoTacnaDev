const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgres://postgres.fhdnwwqiraybpakspegx:EcoTacnaJPA22@aws-1-us-east-1.pooler.supabase.com:6543/postgres',
  ssl: { rejectUnauthorized: false }
});

async function runQueries() {
  try {
    await client.connect();
    console.log("=== DB CONNECTED ===");

    let res = await client.query('SELECT COUNT(*) AS total_companies FROM companies;');
    console.log("1. Total companies:", res.rows[0]);

    res = await client.query(`
      SELECT company_type, subscription_status, COUNT(*) 
      FROM companies 
      GROUP BY company_type, subscription_status 
      ORDER BY company_type, subscription_status;
    `);
    console.log("2. Companies by type/status:");
    console.table(res.rows);

    res = await client.query('SELECT id, company_id, status, current_period_start, current_period_end FROM subscriptions ORDER BY id DESC LIMIT 5;');
    console.log("3. Subscriptions (Top 5):");
    console.table(res.rows);

    res = await client.query('SELECT id, subscription_id, amount, status, created_at FROM payments ORDER BY id DESC LIMIT 5;');
    console.log("4. Payments (Top 5):");
    console.table(res.rows);

    res = await client.query('SELECT id, status, estado_pago, approximate_volume_liters, precio_ofertado_por_litro, litros_confirmados, monto_total FROM pickup_requests ORDER BY id DESC LIMIT 5;');
    console.log("5. Pickup Requests (Top 5):");
    console.table(res.rows);

    res = await client.query(`
      SELECT SUM(amount) AS sum_amount 
      FROM payments 
      WHERE status IN ('APROBADO', 'PAGADO', 'COMPLETADO') 
      AND created_at >= date_trunc('month', CURRENT_DATE);
    `);
    console.log("6. SUM Payments (Current month):", res.rows[0]);

    res = await client.query(`
      SELECT SUM(litros_confirmados) AS sum_litros 
      FROM pickup_requests 
      WHERE status IN ('COMPLETADO') OR estado_pago IN ('PAGADO');
    `);
    console.log("7. SUM Litros Confirmados:", res.rows[0]);

  } catch (err) {
    console.error("DB Error:", err);
  } finally {
    await client.end();
  }
}

runQueries();
