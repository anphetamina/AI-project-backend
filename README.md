# API documentation

- [API documentation](#api-documentation)
  - [/students](#students)
  - [/teachers](#teachers)
  - [Requests body](#requests-body)
    - [Student request](#student-request)
    - [Teacher request](#teacher-request)
  - [Response entities](#response-entities)
    - [Student entity](#student-entity)
    - [Teacher entity](#teacher-entity)
    - [Course entity](#course-entity)
    - [Team entity](#team-entity)
    - [Assignment entity](#assignment-entity)
  - [Team service](#team-service)
  - [Virtual Machine service](#virtual-machine-service)
  - [Exercise and Assignment service](#exercise-and-assignment-service)
  - [Notification service](#notification-service)

## /students

| Use case | Operation | URL | Roles | Request | Response |
|---|---|---|---|---|---|
| Get a student | GET | /{id} | Student | - | [Student](#student-entity) |
| Get all students | GET | / | - | - | [Student](#student-entity) list |
| Get courses in which a student is enrolled | GET | /{id}/courses | Student / Teacher | - | [Course](#course-entity) list |
| Get teams to which a student is part of | GET | /{id}/teams | Student | - | [Team](#team-entity) list |
| Get student assignments | GET | /{id}/assignments | Student / Teacher? | - | [Assignment](#assignment-entity) list |
| Add new student | POST | / | ? | [Student request](#student-request) | [Student](#student-entity) |

## /teachers

| Use case | Operation | URL | Roles | Request | Response |
|---|---|---|---|---|---|
| Get a teacher | GET | /{id} | Teacher | - | [Teacher](#teacher-entity) |
| Get courses taught by a teacher | GET | /{id}/courses | Teacher | - | [Course](#course-entity) |
| Add a new teacher | POST | / | ? | [Teacher request](#teacher-request) | [Teacher](#teacher-entity) |

## /courses

| Use case | Operation | URL | Roles | Request | Response |
|---|---|---|---|---|---|
| Get a course | GET | /{id} | Teacher | - | [Course](#course-entity) |
| Get all courses | GET | / | ? | - | [Course](#course-entity) list |
| Get a team | GET | /{id}/teams/{id} | ? | - | [Team](#team-entity) |
| Get enrolled students | GET | /{id}/enrolled | Teacher | - | [Student](#student-entity) list |
| Get members of a team | GET | /{id}/teams/{id}/members | Teacher / Student | - | [Student](#student-entity) list |
| Get all teams | GET | /{id}/teams | Teacher | - | [Team](#team-entity) list |
| Get course teachers | GET | /{id}/teachers | Teacher | - | [Teacher](#teacher-entity) list |
| Delete a course | DELETE | /{id} | ? | - | - |
| Update a course | PUT | /{id} | Teacher | [Course request](#course-request) | [Course](#course-entity) |
| Add a new course | POST | / | Teacher | [Course request](#course-request) | [Course](#course-entity) |
| Get students already part of a team | GET | /{id}/teams/students | ? | - | [Student](#student-entity) list |

## Request types

### Student request

| Name | Type | Description |
|---|---|---|
| id | String |  |
| name | String |  |
| firstName | String |  |
| image | MultipartFile | A default image is added during the registration phase |

### Teacher request

| Name | Type | Description |
|---|---|---|
| id | String |  |
| name | String |  |
| firstName | String |  |
| image | MultipartFile | A default image is added during the registration phase |

### Course request

| Name | Type | Description |
|---|---|---|
| id | String |  |
| name | String |  |
| min | int | Minimum number of students per team |
| max | int | Maximum number of students per team |
| enabled | Boolean |  |

## Response entities

### Student entity

| Name | Type | Description |
|---|---|---|
| id | String |  |
| name | String |  |
| firstName | String |  |
| email | String |  |
| image | Byte[] |  |

### Teacher entity

| Name | Type | Description |
|---|---|---|
| id | String |  |
| name | String |  |
| firstName | String |  |
| email | String |  |
| image | Byte[] |  |

### Course entity

| Name | Type | Description |
|---|---|---|
| id | String |  |
| name | String |  |
| min | int | Minimum number of students per team |
| max | int | Maximum number of students per team |
| enabled | Boolean |  |

### Team entity

| Name | Type | Description |
|---|---|---|
| id | Long |  |
| name | String |  |
| firstName | String |  |
| status | TeamStatus | UNCONFIRMED, ACTIVE |

### Assignment entity

| Name | Type | Description |
|---|---|---|
| id | Long |  |
| published | Timestamp |  |
| status | AssignmentStatus |  |
| flag | boolean |  |
| score | Integer |  |
| image | Byte[] |  |

---
to be removed

## Team service
| Use case | Operation | URL | Roles | Request body | Request params | Notes |
|---|---|---|---|---|---|---|
|Get team|GET|/API/courses/{courseId}/teams/{teamId}|Student/Teacher|-|-|-|
|Get team members|GET|/API/courses/{courseId}/teams/{teamId}/members|Student|-|-|-|
|Get students in team|GET|/API/courses/{courseId}/teams/students|Teacher|-|-|-|
|Get available students|GET|/API/courses/{courseId}/teams/available-students|Student|-|-|A student is not available if he is part of a "completed" team. A team is "completed" only if all proposed students have confirmed their participation|
|Enable course|POST|/API/courses/{courseId}/enable|Teacher|-|-|-|
|Disable course|POST|/API/courses/{courseId}/disable|Teacher|-|-|-|
|Enroll student|POST|/API/courses/{courseId}/enrollOne|Teacher|String studentId|-|-|
|Add teacher to a course|POST|/API/courses/{courseId}/teachers|Teacher|String teacherId|-|-|
|Add and enroll students|POST|/API/courses/{courseId}/enrollMany|Teacher|csv file|-|-|
|Enroll students|POST|/API/courses/{courseId}/enrollAll|Teacher|csv file|-|-|
|Create team|POST|/API/courses/{courseId}/teams|Student|String teamName<br>List\<String\> memberIds|-|-|
|Get all courses|GET|/API/courses|-|-|-|-|
|Get course|GET|/API/courses/{courseId}|Teacher|-|-|-|
|Get enrolled students|GET|/API/courses/{courseId}/enrolled|Teacher|-|-|-|
|Get teams|GET|/API/courses/{courseId}/teams|Teacher|-|-|-|
|Get teachers|GET|/API/courses/{courseId}/teachers|Teacher|-|-|-|
|Create course|POST|/API/courses|Teacher|CourseDTO course|-|-|
|Delete a course|DELETE|/API/courses/{courseId}|Teacher|-|-|-|
|Update name course|PUT|/API/courses/{courseId}|Teacher|String name|-|-|
|Update course|PUT|/API/courses/{courseId}/setCourse|Teacher|String name <br>String min <br>String max <br>String enabled|-|-|

## Virtual Machine service

| Use case | Operation | URL | Roles | Request body | Request params | Notes |
|---|---|---|---|---|---|---|
|Get virtual machine|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}|Student/Teacher|-|-|-|
|Get virtual machine owners|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}/owners|Student/Teacher|-|-|-|
|Get virtual machine model|GET|/API/courses/{courseId}/model|Student/Teacher|-|-|-|
|Create virtual machine|POST|/API/courses/{courseId}/teams/{teamId}/virtual-machines|Student|String studentId<br>int numVcpu<br>int diskSpace<br>int ram|-|-|
|Delete virtual machine|DELETE|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}|Student|-|-|-|
|Update virtual machine|PUT|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}|Student|-|-|-|
|Turn on virtual machine|POST|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}/on|Student|-|-|-|
|Turn off virtual machine|POST|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}/off|Student|-|-|-|
|Get virtual machines|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines|Student|-|-|-|
|Share virtual machine ownership|POST|/API/courses/{courseId}/teams/{teamId}/virtual-machines/{vmId}/owners|Student|String studentId|-|-|
|Create configuration|POST|/API/courses/{courseId}/teams/{teamId}/configuration|Teacher|int min_vcpu<br>int max_vcpu<br>int min_disk_space<br>int max_disk_space<br>int min_ram<br>int max_ram<br>int max_on<br>int tot|-|-|
|Update configuration|PUT|/API/courses/{courseId}/teams/{teamId}/configuration|Teacher|-|-|-|
|Create virtual machine model|POST|/API/courses/{courseId}/model|Teacher|SystemImage os|-|-|
|Delete virtual machine model|DELETE|/API/courses/{courseId}/model|Teacher|-|-|By deleting a VM model, all the related virtual machines are deleted as well|
|Get virtual machine model|GET|/API/courses/{courseId}/model|Teacher|-|-|-|
|Get configuration|GET|/API/courses/{courseId}/teams/{teamId}/configuration|Teacher|-|-|-|
|Get CPU (cores) in use|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/active-cpu|Student/Teacher|-|-|-|
|Get disk space (MB) in use|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/active-disk-space|Student/Teacher|-|-|-|
|Get RAM (GB) in use|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/active-ram|Student/Teacher|-|-|-|
|Get total virtual machines number|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/tot|Student/Teacher|-|-|-|
|Get active virtual machines number|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/tot-on|Student/Teacher|-|-|-|
|Get total/active resources|GET|/API/courses/{courseId}/teams/{teamId}/virtual-machines/resources|Student/Teacher|-|-|-|


## Exercise and Assignment service
Tutte le immagini sono state salvate come array di byte nel db.(Se seguendo questo https://medium.com/@rameez.s.shaikh/upload-and-retrieve-images-using-spring-boot-angular-8-mysql-18c166f7bc98 dovresti riuscire a ricomporre l'immagine)

| Use case | Operation | URL | Roles | Request body |Request params|Notes|
|---|---|---|---|---|---|---|
|Get exercise by id|GET|/API/courses/{courseId}/{exerciseId}|Student/Teacher|-|-|-|
|Get last assignments for all student enrolled to the course|GET|/API/courses/{courseId}/{exerciseId}/assignments|Teacher|-|-|-|
|Get the history of the assignment for a student|GET|/API/courses/{courseId}/{exerciseId}/history|Teacher/Student ony for his id|-|-|-|
|Add assignment state equal to null|POST|/API/courses/{courseId}/{exerciseId}/assignmentNull|da fare in maniera automatica dopo che viene caricato un exercise|-|-|-|
|Add assignment state equal to read|POST|/API/courses/{courseId}/{exerciseId}/assignmentRead|Student|String studentId|-|-|
|Add assignment for exercise|POST|/API/courses/{courseId}/{exerciseId}/assignmentSubmit|Student|String studentId|image=MultipartFile file|Lo studente può caricare solo una soluzione prima che il docente gli dia il permesso per rifralo|
|Add review for assignment|POST|/API/courses/{courseId}/{exerciseId}/assignmentReview|Teacher|String studentId <br>String flag<br>String voto|image=MultipartFile file| Il voto viene richiesto solo se il flag=false e dunque l'elaborato e definitivo, se il falg=true l'elaborato dovrà essere letto e consegnato dallo studente|
|Add exercise for course|POST|/API/courses/{courseId}/createExercise|Teacher|String expired|image=MultipartFile file|-|
|Get exercise for course|GET|/API/courses/{courseId}/exercises|Teacher/Student|-|-|-|
|Get assignment for id|GET|/API/courses/{courseId}/{assignmentId}|Teacher/Student|-|-|-|
|Get all assignments for student|GET|/API/students/{studentId}/assignments|Teacher/Student|-|-|-|

Nella classe ScheduledTasks c'è un metodo che parte in maniera automatica alle 4.30 per aggiungere lo stato consegnato agli elaborati non consegnati prima della scadenza della consega



## Notification service
| Use case | Operation | URL | Roles | Request body | Request params | Notes |
|---|---|---|---|---|---|---|
|Confirm token|GET|/API/notifications/confirm/{token}|Student|-|-|-|
|Reject token|GET|/API/notifications/reject/{token}|Student|-|-|-|
|~~Get unexpired tokens by team~~|GET|/API/teams/{teamId}/tokens|Student|-|-|-|
|Get unexpired tokens by student|GET|/API/courses/{courseId}/enrolled/{studentId}/tokens|Student|-|-|-|


[#student]: #student
