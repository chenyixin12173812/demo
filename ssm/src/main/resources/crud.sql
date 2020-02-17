
DROP TABLE  IF EXISTS tb_emp;
CREATE TABLE tb_emp (
emp_id INT(11) NOT NULL AUTO_INCREMENT,
emp_name varchar(255) NOT NULL,
gender CHAR(1),
d_id varchar(11),
primary key (emp_id),
key (d_id)
);

DROP TABLE IF EXISTS tb_dept;
CREATE TABLE tb_dept(
  dept_id varchar(11),
  dept_name varchar(255),
  primary key (dept_id),
  foreign key (dept_id) references tb_emp(d_id)
);


insert into tb_emp values (1,'chen','m',1);
insert into tb_emp values (2,'yi','m',2);
insert into tb_emp values (3,'xin','s',3);


insert into tb_dept values (1,'dept1');
insert into tb_dept values (2,'dept2');
insert into tb_dept values (3,'dept3');