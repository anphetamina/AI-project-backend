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
