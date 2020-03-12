alter table code_description rename column description to description_fi;
alter table code_description add column description_en CHARACTER VARYING(256);

update code_description set description_en = 'Traffic jam' where code = '133';
update code_description set description_en = 'Roadwork' where code = '142';
update code_description set description_en = 'Slippery road' where code = '144';
update code_description set description_en = 'Traffic lights' where code = '165';
update code_description set description_en = 'Wind' where code = '183';
update code_description set description_en = 'Other danger' where code = '189';

alter table code_description alter column description_en SET NOT NULL;
