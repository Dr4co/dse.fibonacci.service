dse.fibonacci.service
=====================
Börja med att installera relevanta bundles.<br>

<b>OSGi></b> install file:C:/plugins/dse.FibonacciInterface_1.0.0. 201308012258.jar<br>
Bundle id is 14

<b>OSGi></b> install file:C:/plugins/dse.FibonacciService_1.0.0. 201308012259.jar<br>
Bundle id is 15

<b>OSGi></b> install file:C:/plugins/dse.FibonacciClientA_1.0.0.201308012300.jar<br>
Bundle id is 16

<b>OSGi></b> install file:C:/plugins/dse.FibonacciClientB_1.0.0. 201308012301.jar<br>
Bundle id is 17

För att starta skriver man ”start” i konsollen och önskat Bundle-Id, t.ex.<br>
start 14 för att starta bundle interface (från ovanstående installation)<br>
start 15 för att starta Server service (från ovanstående installation)<br>
start 16 för att starta Client-A service (från ovanstående installation)<br>
start 17 för att starta Client-B service (från ovanstående installation)<br>

<h4>Resultat:</h4>
<b>OSGi></b> start 15<br>
Server: First run<br>
<b>OSGi></b> start 16<br>
Client-A: 0<br>
Client-A: 1<br>
Client-A: 1<br>
Client-A: 2<br>
<b>OSGi></b> start 17<br>
Client-B: 3<br>
Client-B: 5<br>
Client-B: 8<br>
Client-B: 13<br>
Client-A: 21<br>
Client-A: 34<br>
Client-A: 55<br>
Client-A: 89<br>
osv…<br>

GitHub URL: https://github.com/Dr4co/<br>
<i>dse.fibonacci.service</i><br>
<i>dse.fibonacci.clientA</i><br>
<i>dse.fibonacci.clientB</i><br>
<i>dse.fibonacci.interface</i><br>

<h5>Möjliga vidare utvecklingar</h5>
Med hjälp av konfigurationsmöjligheterna i OSGi kan man styra hur många tal ur sekvensen som varje klient ska hämta.
Detta gör man genom att sätta sätta en s.k. property.

<b>OSGi></b> setp dse.fibonacci.service.fibsize=1<br>
Ändrar så att property "dse.fibonacci.service.fibsize" sätts till värdet 1.<br>
Vilket innebär att varje klient enbart kommer att hämta ett tal åt gången och<br>
skriva ut resultatet direkt i System.out. Det ska givetvis gå att sätta till<br>
ett lämligt värde. Standardvärdet är satt till fyra.<br>
<br>
De två sista beräknade fibonacci talen lagras i ett "File" objekt med namn:
"fibsequence.dat".
