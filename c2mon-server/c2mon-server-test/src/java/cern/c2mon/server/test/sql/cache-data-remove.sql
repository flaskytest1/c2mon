delete from property;
delete from deviceproperty;
delete from command;
delete from devicecommand;
delete from device;
delete from deviceclass;
delete from alarm;
delete from datatag where tagcontroltag=0;
delete from commandtag;
delete from equipment where eq_parent_id is not null;
delete from equipment;
delete from process;
delete from datatag where tagcontroltag=1;