create database eazybank;

use eazybank;

create table `users`
(
    `id`       INT         NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(45) NOT NULL,
    `password` VARCHAR(45) NOT NULL,
    `enabled`  INT         NOT NULL,
    PRIMARY KEY (`id`)
);

create table `authorities`
(
    `id`        INT         NOT NULL AUTO_INCREMENT,
    `username`  VARCHAR(45) NOT NULL,
    `authority` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id`)
);

insert ignore into `users`
values (null, 'happy', '12345', '1');
insert ignore into `authorities`
values (null, 'happy', 'write');

create table `customer`
(
    `id`    int          not null auto_increment,
    `email` varchar(45)  not null,
    `pwd`   varchar(200) not null,
    `role`  varchar(45)  not null,
    primary key (`id`)
);

insert into `customer` (email, pwd, role)
values ('johndoe@example.com', '54321', 'admin');