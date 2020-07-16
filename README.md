# Virtual Machine service

| Use case | Operation | URL | 
|---|---|---|
|Create VM|POST|/virtual-machines|
|Delete VM|DELETE|/virtual-machines|
|Update VM|PUT|/virtual-machines|
|Turn on VM|POST|/virtual-machines/{vmId}|
|Turn off VM|POST|/virtual-machines/{vmId}|
|Get VM for a team|GET|/teams/{teamId}/virtual-machines/{vmId}|
|Get VMs for a team|GET|/teams/{teamId}/virtual-machines|
|Share VM ownership|POST|/teams/{teamId}/virtual-machines/{vmId}/owners|
|Remove VM ownership|POST|/teams/{teamId}/virtual-machines/{vmId}/owners|
|Create VM configuration for a team|POST|/teams/{teamId}/configuration|
|Update VM configuration for a team|PUT|/teams/{teamId}/configuration|
|Create VM model for a course|POST|/courses/{courseName}/model|
|Delete VM model for a course|DELETE|/courses/{courseName}/model|
|Get VM model for a course|GET|/courses/{courseName}/model|
|Get CPU in use by team|GET|/teams/{teamId}/virtual-machines/cpu|
|Get disk space in use by team|GET|/teams/{teamId}/virtual-machines/disk-space|
|Get RAM in use by team|GET|/teams/{teamId}/virtual-machines/ram|
|Get total VM number by team|GET|/teams/{teamId}/virtual-machines/tot|
|Get max active VM number by team|GET|/teams/{teamId}/virtual-machines/max-on|
||||
||||

