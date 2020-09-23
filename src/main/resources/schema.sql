create table configuration
(
    id             bigint not null
        primary key,
    max_disk_space int    not null,
    max_on         int    not null,
    max_ram        int    not null,
    max_vcpu       int    not null,
    min_disk_space int    not null,
    min_ram        int    not null,
    min_vcpu       int    not null,
    tot            int    not null
);

create table confirmation_token
(
    id          varchar(255) not null
        primary key,
    expiry_date datetime     null,
    username    varchar(255) null
);

create table hibernate_sequence
(
    next_val bigint null
);

create table jwt_black_list
(
    id varchar(255) not null
        primary key
);

create table student
(
    id         varchar(255) not null
        primary key,
    email      varchar(255) null,
    first_name varchar(255) null,
    image      longblob     null,
    last_name  varchar(255) null
);

create table teacher
(
    id              varchar(255) not null
        primary key,
    email           varchar(255) null,
    first_name      varchar(255) null,
    last_name       varchar(255) null,
    profile_picture longblob     null
);

create table token
(
    id          varchar(255) not null
        primary key,
    expiry_date datetime     null,
    status      int          null,
    student_id  varchar(255) null,
    team_id     bigint       null
);

create table user
(
    id       varchar(255) not null
        primary key,
    email    varchar(255) null,
    enabled  bit          not null,
    password varchar(255) null
);

create table user_roles
(
    user_id varchar(255) not null,
    roles   varchar(255) null,
    constraint FK55itppkw3i07do3h7qoclqd4k
        foreign key (user_id) references user (id)
);

create table virtual_machine_model
(
    id bigint not null
        primary key,
    os int    null
);

create table course
(
    id       varchar(255) not null
        primary key,
    enabled  bit          not null,
    max      int          not null,
    min      int          not null,
    name     varchar(255) null,
    model_id bigint       null,
    constraint FK78em184hl9wuqerswxsh6rj8o
        foreign key (model_id) references virtual_machine_model (id)
);

create table assignment
(
    id        bigint       not null
        primary key,
    expired   datetime     null,
    image     longblob     null,
    published datetime     null,
    course_id varchar(255) null,
    constraint FKrop26uwnbkstbtfha3ormxp85
        foreign key (course_id) references course (id)
);

create table paper
(
    id            bigint       not null
        primary key,
    flag          bit          not null,
    image         longblob     null,
    published     datetime     null,
    score         varchar(255) null,
    status        int          null,
    assignment_id bigint       null,
    student       varchar(255) null,
    constraint FKdhohyyrdlkypueydqe97lnweu
        foreign key (student) references student (id),
    constraint FKl3jkbqdov3q7imp3vjbq15qxg
        foreign key (assignment_id) references assignment (id)
);

create table student_course
(
    student_id varchar(255) not null,
    course_id  varchar(255) not null,
    constraint FKejrkh4gv8iqgmspsanaji90ws
        foreign key (course_id) references course (id),
    constraint FKq7yw2wg9wlt2cnj480hcdn6dq
        foreign key (student_id) references student (id)
);

create table teacher_course
(
    teacher_id varchar(255) not null,
    course_id  varchar(255) not null,
    constraint FKaleldsg7yww5as540ld8iwghe
        foreign key (teacher_id) references teacher (id),
    constraint FKp8bco6842vkqh13y4759ib7tk
        foreign key (course_id) references course (id)
);

create table team
(
    id            bigint       not null
        primary key,
    name          varchar(255) null,
    status        int          null,
    configuration bigint       null,
    course_id     varchar(255) null,
    constraint FK4h8unn499pay21jhsa3ds344l
        foreign key (configuration) references configuration (id),
    constraint FKrdbahenwatuua698jkpnfufta
        foreign key (course_id) references course (id)
);

create table team_student
(
    team_id    bigint       not null,
    student_id varchar(255) not null,
    constraint FKcikvw8vwdt6jmeyksh25q60q
        foreign key (student_id) references student (id),
    constraint FKin4tsinuxmguuh6qvtue7oyti
        foreign key (team_id) references team (id)
);

create table virtual_machine
(
    id         bigint not null
        primary key,
    disk_space int    not null,
    num_vcpu   int    not null,
    ram        int    not null,
    status     int    null,
    team_id    bigint null,
    model_id   bigint null,
    constraint FKatbrl5j5yrj289tq4wmdu4w8w
        foreign key (model_id) references virtual_machine_model (id),
    constraint FKh68elpgbi6sw7hn2mg4audwjx
        foreign key (team_id) references team (id)
);

create table student_vm
(
    student_id varchar(255) not null,
    vm_id      bigint       not null,
    constraint FK29a7iua8rsd3wj0cgf7jwmo7l
        foreign key (vm_id) references virtual_machine (id),
    constraint FKtj066j46qilog8ew89nipbq3u
        foreign key (student_id) references student (id)
);


