-- CreateTable
CREATE TABLE `User` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `internal_id` VARCHAR(191) NOT NULL,
    `username` VARCHAR(191) NOT NULL,
    `top_artists` JSON NOT NULL,
    `top_genres` JSON NOT NULL,
    `currently_listening` VARCHAR(191) NULL,
    `is_banned` BOOLEAN NOT NULL DEFAULT false,
    `pfp_url` VARCHAR(191) NULL,
    `bio` VARCHAR(191) NULL,

    UNIQUE INDEX `User_internal_id_key`(`internal_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `Connection` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `user_id_1` INTEGER NOT NULL,
    `user_id_2` INTEGER NOT NULL,
    `match_percent` INTEGER NULL,
    `are_friends` BOOLEAN NOT NULL DEFAULT false,
    `user_1_requested` BOOLEAN NULL,

    UNIQUE INDEX `Connection_user_id_1_user_id_2_key`(`user_id_1`, `user_id_2`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `Session` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `Report` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `offending_user_id` INTEGER NOT NULL,
    `reporting_user_id` INTEGER NOT NULL,
    `reason` ENUM('OFFENSIVE_LANGUAGE', 'PLAYLIST_ABUSE', 'SPAMING_CHAT', 'OTHER') NOT NULL,
    `reason_text` VARCHAR(191) NULL,
    `report_context` JSON NOT NULL,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `User` ADD CONSTRAINT `User_id_fkey` FOREIGN KEY (`id`) REFERENCES `Session`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Connection` ADD CONSTRAINT `Connection_user_id_1_fkey` FOREIGN KEY (`user_id_1`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Connection` ADD CONSTRAINT `Connection_user_id_2_fkey` FOREIGN KEY (`user_id_2`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Report` ADD CONSTRAINT `Report_offending_user_id_fkey` FOREIGN KEY (`offending_user_id`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Report` ADD CONSTRAINT `Report_reporting_user_id_fkey` FOREIGN KEY (`reporting_user_id`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
