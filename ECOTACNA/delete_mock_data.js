const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://postgres.fhdnwwqiraybpakspegx:EcoTacnaJPA22@aws-1-us-east-1.pooler.supabase.com:6543/postgres',
  ssl: { rejectUnauthorized: false }
});

async function run() {
  await client.connect();
  const ids = [26,27,28,29,30,31,32,33,34,35,36,37,38,39,40].join(',');

  try {
    await client.query('BEGIN');
    
    // 1. Delete audit_logs
    const auditRes = await client.query(`DELETE FROM audit_logs WHERE user_id IN (SELECT id FROM users WHERE company_id IN (${ids}))`);
    console.log(`Deleted ${auditRes.rowCount} audit logs`);

    // 4. Delete payments
    const payRes = await client.query(`DELETE FROM payments WHERE company_id IN (${ids})`);
    console.log(`Deleted ${payRes.rowCount} payments`);

    // 5. Delete subscriptions
    const subRes = await client.query(`DELETE FROM subscriptions WHERE company_id IN (${ids})`);
    console.log(`Deleted ${subRes.rowCount} subscriptions`);

    // 6. Delete users
    const usrRes = await client.query(`DELETE FROM users WHERE company_id IN (${ids})`);
    console.log(`Deleted ${usrRes.rowCount} users`);

    // 7. Delete companies
    const compRes = await client.query(`DELETE FROM companies WHERE id IN (${ids})`);
    console.log(`Deleted ${compRes.rowCount} companies`);

    await client.query('COMMIT');
    console.log('Cleanup committed successfully.');

  } catch (err) {
    await client.query('ROLLBACK');
    console.error('Error occurred, rolled back:', err);
  } finally {
    await client.end();
  }
}

run();
