#### Dobriță George-Mădălin - 334CA ####
## Tema #2 APD -  Procesarea de documente folosind paradigma Map-Reduce

### Structura proiectului
Proiectul are urmatoarea structură:
* constants:
  * **Constants**: clasă cu cateva constante(pentru înfrumusetarea codului)
* fibo:
  * **Fibonacci**: singleton; conține o listă de întregi; întregul de pe poziția i conține
numărul Fibbonacci(i); lista este inițializată cu 0 și 1, iar atunci când se cere
elementul al i-lea, se returnează dacă există, dacă nu, *se calculează toate elementele
inexistente de dinaintea acestuia, inclusiv acest element*, iar apoi se returnează
* parser:
  * **Parser**: centrul programului
* result:
  * **Result**: o clasă ce conține rezultatele rulării unui task de map
  * **FinalResult**: o clasă ce conține rezultatele rulării unui task de reduce,
rezultate ce trebuiesc scrise în fișierul de output
* task:
  * **MapReduce**: clasă abstractă, implemetată separat mai apoi de Map și Reduce;
conține câmpurile comune ale celor 2 clase 
  * **Map**: realizează operațiile unui task map
  * **Reduce**: realizează operațiile unui task reduce
  * **Print**: un nou tip de task, care doar printează rezultatele task-urilor de reduce
în fisier
* **Tema2**: creează un obiect Parser care parsează fisierul de input și începe rularea
task-urilor

### Logica proiectului
Entry-point-ul programului este în main-ul din Tema2. Dacă programul nu se rulează cu
parametrii corespunzători, acesta moare; altfel, se creează o instanța de Parser ce
cunoaște toate informațiile date ca parametrii în CLI. <br><br>
Prima oară, se creează(în constructor) un pool care are atația workeri cât s-au dat în
CLI, iar apoi se parsează fișierul, rețiând mărimea fiecarui block de caractere ce va
fi prelucrat de un task de map, numarul de fișiere, iar pentru fiecare fișier in parte,
se va crea un *task reduce default(care are inițializat doar numele fișierului pe care
lucrează)*, care se va adăuga într-o *listă de task-uri aflate în așteptare*. Pentru
fiecare block din fiecare fișier se creează câte un task map cu informațiile
corespunzătoare, iar aceste task-uri se adaugă *într-o listă de task-uri inițiale*.
Pentru fiecare task creat, de tip map și reduce, se reține numărul lor total în două
variabile de tip atomic(*două cozi - rețin câte task-uri de map/reduce sunt active sau
în așteptare la un moment dat*). <br><br>
Mai apoi, *pool-ului de taskuri îi este submitat fiecare task inițial(de tip map)*. <br>
<br>
Fiecare task de tip map primește numele fișierului, block-size-ul, offset-ul la care
începe fragmentul de text, mărimea fișierului în bytes și alte câmpuri din MapReduce.
<br><br>
Task-urile de tip map, *ajustează mărimea și offset-ul* fragmentului de caractere dacă
*începe sau se termină în mijlocul unui caracter*. Dacă începe cu un delimitator sau în
mijlocul unui cuvânt(primul caracter al secvenței este literă sau cifră, la fel și cel
de dinaintea sa), atunci *offset-ul crește, iar block-size-ul scade*(ca să rămână capătul
din dreapta al fragmentului același), până când nu se mai repetă vreuna din condiții.
Dacă se termină în mijlocul unui cuvânt(ultimul caracter al secvenței este literă sau
cifră, la fel și cel de după el), atunci *block-size-ul crește*. <br><br>
Se citește tot fragmentul și se împarte în cuvinte; pentru fiecare cuvânt, se calculează
*numărul de caractere* și dacă există în map vreo intrare cu această cheie, se updatează, 
crește cu 1(mai există încă un cuvânt cu aceeași lungime față de înainte), altfel,
se adaugă o intrare nouă cu valoarea 1(un singur cuvânt cu lungimea aceasta momentan).
Tot pentru fiecare cuvânt, dacă este de lungime maximă, se adaugă la lista cu cuvinte
de lungime maximă; dacă este mai mare ca lungimea maximă, se updatează lungimea maximă,
iar lista va avea un singur cuvânt(acesta curent). <br><br>
La finalul unui task map, *se decrementează coada internă de task-uri de tip map* și se
creează un *rezultat* cu numele fișierului, map-ul și lista despre care s-a discutat mai sus și
numarul de cuvinte din acel fragment. Acest rezultat *se trimite thread-ului principal*
(obiectul parser), care îl adaugă *task-ului reduce corespunzător*(cu același nume al
fișierului). Task-ul reduce își *updateazp în mod sincronizat* lista de map-uri de forma
celor de mai sus și lista de liste de forma celor de mai sus și tot sincronizat se
crește numărul de cuvinte din acel fișier cu numărul de cuvinte din acel fragment
asupra căruia s-a executat task-ul de map. <br><br>
Dacă coada atomica de task-uri de tip map are contorul intern 0(operație verificată în
mod sincronizat), înseamnă că toate task-urile map s-au executat, deci parser-ul adaugă
în pool-ul de task-uri toate task-urile de tip reduce. <br><br>
Task-urile de tip reduce au *2 etape(3 cu cea finală)*: în *etapa de combinare*, lista de
map-uri se strânge într-un singur map(un map ca cel din task-ul map, dar pe tot documentul, nu
doar pe un fragment), iar lista de liste se strânge *într-o listă unică de cuvinte de
lungime maximă*; în *etapa de procesare*, se calculeazî *rang-ul* fișierului dupa formula
din enunț; în etapa finală, *se decrementează coada internă de task-uri de tip reduce*
și se creează un *rezultat* cu numele fișierului, rank-ul, numărul de cuvinte de lungime
maximă și lungimea celui mai mare cuvânt care *se transmite unei liste de rezultate finale
din parser*. <br><br>
Dacă contorul intern al cozii ce contorizeaza task-urile de tip reduce ajunge la 0,
*se adaugă în pool-ul de task-uri un task de tip print* care are rolul de *a scrie în
fișierul de output lista de rezultate finală din parser*(dupa ce o ordonează descrescător
după rang) la care contribuie fiecare task reduce. <br><br>

#### Notă subsol
Înainte de a arunca un ochi pe codul scris, recomand citirea structurii și logicii
proiectului, iar dacă nu este ceva clar, în cod este explicat amănunțit ce face fiecare
instrucțiune, deci dacț se întelege cum se îmbină clasele, totul devine foarte usor.
<br> ENJOY!