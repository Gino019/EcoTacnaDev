const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://postgres.fhdnwwqiraybpakspegx:EcoTacnaJPA22@aws-1-us-east-1.pooler.supabase.com:6543/postgres',
  ssl: {
    rejectUnauthorized: false
  }
});

async function run() {
  await client.connect();
  const query = `
    SELECT 
      c.id AS company_id,
      c.ruc,
      c.business_name,
      c.company_type,
      c.subscription_status,
      c.created_at,
      u.id AS user_id,
      u.email,
      u.phone,
      u.role,
      u.enabled
    FROM companies c
    LEFT JOIN users u ON u.company_id = c.id
    WHERE c.company_type = 'RECOLECTORA'
      AND (
        UPPER(COALESCE(c.business_name, '')) LIKE '%INFORMACION NO DISPONIBLE%'
        OR UPPER(COALESCE(c.business_name, '')) LIKE '%INFORMACIÓN NO DISPONIBLE%'
        OR LOWER(COALESCE(u.email, '')) LIKE 'recolector_%@test.com'
        OR LOWER(COALESCE(u.email, '')) LIKE '%@test.com'
        OR LOWER(COALESCE(u.email, '')) LIKE '%recolector.com'
        OR COALESCE(u.phone, '') = '999'
        OR COALESCE(u.phone, '') = 'Información no disponible'
        OR COALESCE(u.phone, '') = 'Informacion no disponible'
      )
    ORDER BY c.id;
  `;
  try {
    const res = await client.query(query);
    console.log("=== BASURA SOSPECHOSA ===");
    console.table(res.rows);
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}

run();
