/*
  Warnings:

  - You are about to drop the column `currently_playing` on the `User` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `User` DROP COLUMN `currently_playing`,
    ADD COLUMN `current_song` VARCHAR(191) NULL,
    ADD COLUMN `current_source` JSON NULL;
