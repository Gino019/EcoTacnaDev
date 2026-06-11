const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://postgres.fhdnwwqiraybpakspegx:EcoTacnaJPA22@aws-1-us-east-1.pooler.supabase.com:6543/postgres',
  ssl: { rejectUnauthorized: false }
});

async function run() {
  await client.connect();
  const ids = [26,27,28,29,30,31,32,33,34,35,36,37,38,39,40].join(',');
  const queries = [
    { name: 'subscriptions', sql: `SELECT * FROM subscriptions WHERE company_id IN (${ids})` },
    { name: 'payments', sql: `SELECT * FROM payments WHERE company_id IN (${ids})` },
    { name: 'transport_units', sql: `SELECT * FROM transport_units WHERE collector_company_id IN (${ids})` },
    { name: 'pickup_requests (collector)', sql: `SELECT * FROM pickup_requests WHERE collector_company_id IN (${ids})` },
    { name: 'pickup_requests (generator)', sql: `SELECT * FROM pickup_requests WHERE company_id IN (${ids})` },
    { name: 'audit_logs (company)', sql: `SELECT * FROM audit_logs WHERE company_id IN (${ids})` },
    { name: 'audit_logs (user)', sql: `SELECT * FROM audit_logs WHERE user_id IN (SELECT id FROM users WHERE company_id IN (${ids}))` }
  ];

  try {
    for (const q of queries) {
      try {
        const res = await client.query(q.sql);
        console.log(`Dependencies in ${q.name}: ${res.rowCount} rows`);
      } catch (err) {
        console.error(`Error in ${q.name}: ${err.message}`);
      }
    }
  } finally {
    await client.end();
  }
}

run();
