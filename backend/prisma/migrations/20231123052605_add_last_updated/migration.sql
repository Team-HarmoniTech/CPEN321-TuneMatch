/*
  Warnings:

  - You are about to alter the column `current_song` on the `User` table. The data in that column could be lost. The data in that column will be cast from `VarChar(191)` to `Json`.
  - You are about to drop the `_friends` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE `_friends` DROP FOREIGN KEY `_friends_A_fkey`;

-- DropForeignKey
ALTER TABLE `_friends` DROP FOREIGN KEY `_friends_B_fkey`;

-- AlterTable
ALTER TABLE `User` ADD COLUMN `last_updated` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    MODIFY `current_song` JSON NULL;

-- DropTable
DROP TABLE `_friends`;

-- CreateTable
CREATE TABLE `Friend` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `requesting_id` INTEGER NOT NULL,
    `requested_id` INTEGER NOT NULL,

    UNIQUE INDEX `Friend_requesting_id_requested_id_key`(`requesting_id`, `requested_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `Friend` ADD CONSTRAINT `Friend_requesting_id_fkey` FOREIGN KEY (`requesting_id`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Friend` ADD CONSTRAINT `Friend_requested_id_fkey` FOREIGN KEY (`requested_id`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
