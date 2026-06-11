const http = require('http');

const loginOptions = {
  hostname: 'localhost',
  port: 8082,
  path: '/ecotacna/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
};

const req = http.request(loginOptions, res => {
  let data = '';
  res.on('data', chunk => { data += chunk; });
  res.on('end', () => {
    if (res.statusCode !== 200) {
      console.log("Login failed", res.statusCode, data);
      return;
    }
    const token = JSON.parse(data).data.token;
    console.log("Token acquired");
    
    // Get Recolectores
    const getRecOptions = {
      hostname: 'localhost',
      port: 8082,
      path: '/ecotacna/api/admin/recolectores',
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json'
      }
    };
    
    const recReq = http.request(getRecOptions, res1 => {
      let recData = '';
      res1.on('data', chunk => { recData += chunk; });
      res1.on('end', () => {
        const recs = JSON.parse(recData).data;
        if (!recs || recs.length === 0) {
          console.log("No recolectores found");
          return;
        }
        const id = recs[0].id;
        console.log("Testing detail for recolector id:", id);
        
        const detailOptions = {
          hostname: 'localhost',
          port: 8082,
          path: `/ecotacna/api/admin/recolectores/${id}/detalle`,
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Accept': 'application/json'
          }
        };
        
        const detailReq = http.request(detailOptions, res2 => {
          let detailData = '';
          res2.on('data', chunk => { detailData += chunk; });
          res2.on('end', () => {
            console.log("DETAIL STATUS:", res2.statusCode);
            console.log("DETAIL BODY:", detailData);
          });
        });
        detailReq.end();
      });
    });
    recReq.end();
  });
});

req.write(JSON.stringify({ email: 'admin_1780000000@test.com', password: 'password123' }));
req.end();
