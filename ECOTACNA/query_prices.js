const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://postgres.fhdnwwqiraybpakspegx:EcoTacnaJPA22@aws-1-us-east-1.pooler.supabase.com:6543/postgres',
  ssl: { rejectUnauthorized: false }
});

async function run() {
  await client.connect();
  const q1 = `
    SELECT s.id, s.company_id, p.name as plan_name, p.monthly_amount as amount, s.status, s.start_date
    FROM subscriptions s
    LEFT JOIN subscription_plans p ON s.plan_id = p.id
    WHERE p.monthly_amount IN (20, 20.00, 45, 45.00, 29.90, 299.90)
    ORDER BY s.id DESC;
  `;
  const q2 = `
    SELECT id, company_id, amount, status, created_at
    FROM payments
    WHERE amount IN (20, 20.00, 45, 45.00, 29.90, 299.90)
    ORDER BY id DESC;
  `;
  try {
    const r1 = await client.query(q1);
    console.log('--- SUBSCRIPTIONS ---');
    console.table(r1.rows);
    const r2 = await client.query(q2);
    console.log('--- PAYMENTS ---');
    console.table(r2.rows);
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}
run();
