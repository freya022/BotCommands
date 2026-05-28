------------------------------------------------------ 10th migration script for BotCommands -----------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

SET SCHEMA 'bc';

UPDATE bc_version
SET version = '3.0.0'
WHERE one_row = true;
