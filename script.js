const $=s=>document.querySelector(s);
const $$=s=>[...document.querySelectorAll(s)];
const VIEWS=['inicio','nosotros','proyecto','materiales','funciona','simulador','datos','proceso','github','equipo','contacto'];
const menu=$('#menu');if(menu)menu.onclick=()=>document.querySelector('nav')?.classList.toggle('open');
function showView(id){
 const target=VIEWS.includes(id)?id:'inicio';
 $$('.spa-view').forEach(section=>{const active=section.id===target;section.classList.toggle('active-view',active);section.setAttribute('aria-hidden',String(!active));});
 $$('nav a').forEach(a=>a.classList.toggle('active',a.getAttribute('href')==='#'+target));
 document.querySelector('nav')?.classList.remove('open');window.scrollTo({top:0,behavior:'instant'});
 if(location.hash!=='#'+target)history.replaceState(null,'','#'+target);
}
$$('a[href^="#"]').forEach(a=>a.addEventListener('click',e=>{const id=a.getAttribute('href').slice(1);if(VIEWS.includes(id)){e.preventDefault();history.pushState(null,'','#'+id);showView(id);}}));
window.addEventListener('popstate',()=>showView(location.hash.slice(1)||'inicio'));
window.addEventListener('hashchange',()=>showView(location.hash.slice(1)||'inicio'));

// La IP no se muestra en la interfaz. Configure la URL real en un proxy/función segura al publicar.
const DATA_ENDPOINT='/api/esp32-data';
let reading=false;
function connectionStatus(text,online=false){
 const state=$('#state'),dot=$('#signalDot');if(state)state.textContent=text;if(dot)dot.classList.toggle('online',online);
}
async function readTelemetry(){
 if(reading)return;reading=true;
 try{
  const response=await fetch(DATA_ENDPOINT,{cache:'no-store',signal:AbortSignal.timeout(4500)});if(!response.ok)throw new Error('HTTP '+response.status);
  const data=await response.json();
  const level=Number(data.humedadSuelo??data.nivel??data.soilMoisture??0);const temp=Number(data.temperatura??data.temperature??0);
  $('#level').textContent=level.toFixed(0)+'%';$('#temp').textContent=temp.toFixed(1)+' °C';$('#liveState').textContent='Datos recibidos';
  $('#time').textContent='Última actualización: '+new Date().toLocaleTimeString('es-EC');connectionStatus('Conectado',true);
 }catch(error){connectionStatus('Sin conexión',false);if($('#liveState'))$('#liveState').textContent='Esperando datos...';}
 finally{reading=false;}
}
readTelemetry();setInterval(readTelemetry,3000);
const repo=$('#repo');if(repo){repo.value='https://github.com/juannnzp/AgroSense-RiegoSmart';repo.readOnly=true;}
showView(VIEWS.includes(location.hash.slice(1))?location.hash.slice(1):'inicio');
