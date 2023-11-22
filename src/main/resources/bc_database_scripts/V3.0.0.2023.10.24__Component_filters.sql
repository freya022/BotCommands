------------------------------------------------------ 3rd migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

set schema 'bc';

update bc_version
set version = '3.0.0-alpha.8'
where one_row = true;

alter table bc_component
    add column filters text array;

update bc_component set filters = '{}';

alter table bc_component alter filters set not null;