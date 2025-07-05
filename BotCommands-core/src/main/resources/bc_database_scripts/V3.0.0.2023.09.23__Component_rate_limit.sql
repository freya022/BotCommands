------------------------------------------------------ 2nd migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

set schema 'bc';

update bc_version
set version = '3.0.0-alpha.6'
where one_row = true;

alter table bc_component
    add column rate_limit_group text null;