# Virtual Machine service

| Use case | Operation | URL | Roles | Request body |
|---|---|---|---|---|
|Get virtual machine|GET|/virtual-machines/{vmId}|Student/Teacher||
|Get VM owners|GET|/virtual-machines/{vmId}/owners|Student/Teacher||
|Get VM model|GET|/virtual-machines/{vmId}/model|Student/Teacher||
|Get VM team|GET|/virtual-machines/{vmId}/team|Student/Teacher||
|Create virtual machine|POST|/virtual-machines|Student|<ul><li>String studentId<li>Long teamId<li>int numVcpu<li>int diskSpace<li>int ram</ul>|
|Delete virtual machine|DELETE|/virtual-machines/{vmId}|Student||
|Update virtual machine|PUT|/virtual-machines/{vmId}|Student|<ul><li>VirtualMachineDTO vm</ul>|
|Turn on virtual machine|POST|/virtual-machines/{vmId}/on|Student||
|Turn off virtual machine|POST|/virtual-machines/{vmId}/off|Student||
|Get virtual machines by team|GET|/teams/{teamId}/virtual-machines|Student||
|Get virtual machines by owner|GET|/students/{studentId}/virtual-machines|Student||
|Share VM ownership|POST|/virtual-machines/{vmId}/owners|Student|<ul><li>String studentId</ul>|
|Create VM configuration|POST|/teams/{teamId}/configuration|Teacher|<ul><li>int min_vcpu<li>int max_vcpu<li>int min_disk_space<li>int max_disk_space<li>int min_ram<li>int max_ram<li>int max_on<li>int tot</ul>|
|Update VM configuration|PUT|/teams/{teamId}/configuration|Teacher|<ul><li>VirtualMachineConfigurationDTO vmc</ul>|
|Create VM model|POST|/courses/{courseName}/model|Teacher|<ul><li>SystemImage os</ul>|
|Delete VM model|DELETE|/courses/{courseName}/model|Teacher||
|Get VM model by course|GET|/courses/{courseName}/model|Teacher||
|Get VM configuration by team|GET|/teams/{teamId}/configuration|Teacher||
|Get CPU (cores) in use|GET|/teams/{teamId}/virtual-machines/active-cpu|Student/Teacher||
|Get disk space (MB) in use|GET|/teams/{teamId}/virtual-machines/active-disk-space|Student/Teacher||
|Get RAM (GB) in use|GET|/teams/{teamId}/virtual-machines/active-ram|Student/Teacher||
|Get total VM number|GET|/teams/{teamId}/virtual-machines/tot|Student/Teacher||
|Get active VM number|GET|/teams/{teamId}/virtual-machines/tot-on|Student/Teacher||
|Get total/active resources|GET|/teams/{teamId}/virtual-machines/resources|Student/Teacher||


# Exercise and Assignemnt Service
Tutte le immagini sono state salvate come array di byte nel db.(Se seguendo questo https://medium.com/@rameez.s.shaikh/upload-and-retrieve-images-using-spring-boot-angular-8-mysql-18c166f7bc98 dovresti riuscire a ricomporre l'immagine)
| Use case | Operation | URL | Roles | Request body |Request param|Scelta implementativa|
|---|---|---|---|---|---|---|
|Get exercise by id|GET| /API/exercises/{exerciseId}|Student/Teacher||||
|Get last assignmets for all student enrolled to the course|GET|/API/exercises/{exerciseId}/assignments|Teacher||||
|Get the history of the assignment for a student|GET|/API/exercises/{exerciseId}/history|Teacher/Student ony for his id||||
|Add assignment state equal to null|POST|/API/exercises/{exerciseId}/assignmentNull|da fare in maniera automatica dopo che viene caricato un exercise||||
|Add assignment state equal to read|POST|/API/exercises/{exerciseId}/assignmentRead|Student|<ul><li>String studentId</ul>|||
|Add assignment for exercise|POST|API/exercises/{exerciseId}/assignmentSubmit|Student|<ul><li>String studentId</ul>|image=MultipartFile file|Lo studente può caricare solo una soluzione prima che il docente gli dia il permesso per rifralo|
|Add reveiw for assignment|POST|API/exercises/{exerciseId}/assignmentReview"|Teacher|<ul><li>String studentId <li>String flag<li>String voto</ul>|image=MultipartFile file| Il voto viene richiesto solo se il flag=false e dunque l'elaborato e definitivo, se il falg=true l'elaborato dovrà essere letto e consegnato dallo studente|
|Add exercise for course|POST|/API/courses/{courseId}/createExercise|Teacher|<ul><li>String expired</ul>|image=MultipartFile file||
|Get exercise for course|GET|/API/courses/{courseId}/exercises|Teacher/Student||||
|Get assignment for id|GET|/API/assignments/{courseId}/{assignmentId}|Teacher/Student|||
|Get all assignmets for student|GET|/API/students/{studentId}/assignments|Teacher/Student|||
Nella classe ScheduledTasks c'è un metodo che parte in maniera automatica alle 4.30 per aggiungere lo stato consegnato agli elaborati non consegnati prima della scadenza della consega

# Course Service
| Use case | Operation | URL | Roles | Request body |
|---|---|---|---|---|
|Delete a course|DELETE|/API/courses/{courseId}|Teacher||
|Updete name course|PUT|/API/courses/{courseId}|Teacher|<ul><li>String name</ul>|
|Update course|PUT|/API/courses/{courseId}/setCourse|Teacher|<ul><li>String name <li>String min <li>String max <li>String enabled</ul>|
