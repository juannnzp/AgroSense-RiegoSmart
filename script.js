const $=s=>document.querySelector(s);
const menu=$('#menu');
if(menu)menu.onclick=()=>document.querySelector('nav')?.classList.toggle('open');
document.querySelectorAll('nav a').forEach(a=>a.onclick=()=>document.querySelector('nav')?.classList.remove('open'));

const eye=$('#eye');
if(eye)eye.onclick=()=>{const ip=$('#ip');if(ip)ip.type=ip.type==='password'?'text':'password'};
let timer;
async function read(){
 const ipInput=$('#ip');if(!ipInput)return;
 let ip=ipInput.value.trim().replace(/\/$/,'');if(!ip)return;
 if(!/^https?:\/\//i.test(ip))ip='http://'+ip;
 try{
  const r=await fetch(ip+'/datos',{cache:'no-store',signal:AbortSignal.timeout(4500)});
  if(!r.ok)throw new Error('HTTP '+r.status);
  const d=await r.json();
  const l=Number(d.humedadSuelo??d.nivel??d.soilMoisture??0);
  const t=Number(d.temperatura??d.temperature??0);
  if($('#level'))$('#level').textContent=l.toFixed(0)+'%';
  if($('#temp'))$('#temp').textContent=t.toFixed(1)+' °C';
  if($('#liveState'))$('#liveState').textContent='Datos recibidos';
  if($('#time'))$('#time').textContent=new Date().toLocaleTimeString('es-EC');
  if($('#state'))$('#state').textContent='● Conectado';
 }catch(e){if($('#state'))$('#state').textContent='● Sin conexión';if($('#liveState'))$('#liveState').textContent='Esperando datos...'}
}
const connect=$('#connect');if(connect)connect.onclick=()=>{read();clearInterval(timer);timer=setInterval(read,3000)};
const repo=$('#repo');if(repo){repo.value='https://github.com/juannnzp/AgroSense-RiegoSmart';repo.readOnly=true;}
