-- CreateTable
CREATE TABLE `User` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `spotify_id` VARCHAR(191) NOT NULL,
    `username` VARCHAR(191) NOT NULL,
    `top_artists` JSON NOT NULL,
    `top_genres` JSON NOT NULL,
    `currently_playing` VARCHAR(191) NULL,
    `is_banned` BOOLEAN NOT NULL DEFAULT false,
    `pfp_url` VARCHAR(191) NULL,
    `bio` VARCHAR(191) NULL,
    `sessionId` INTEGER NULL,
    `connectionComputed` BOOLEAN NOT NULL DEFAULT false,

    UNIQUE INDEX `User_spotify_id_key`(`spotify_id`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `Connection` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `user_id_1` INTEGER NOT NULL,
    `user_id_2` INTEGER NOT NULL,
    `match_percent` INTEGER NOT NULL,

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
    `timestamp` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `_friends` (
    `A` INTEGER NOT NULL,
    `B` INTEGER NOT NULL,

    UNIQUE INDEX `_friends_AB_unique`(`A`, `B`),
    INDEX `_friends_B_index`(`B`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `User` ADD CONSTRAINT `User_sessionId_fkey` FOREIGN KEY (`sessionId`) REFERENCES `Session`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Connection` ADD CONSTRAINT `Connection_user_id_1_fkey` FOREIGN KEY (`user_id_1`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Connection` ADD CONSTRAINT `Connection_user_id_2_fkey` FOREIGN KEY (`user_id_2`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Report` ADD CONSTRAINT `Report_offending_user_id_fkey` FOREIGN KEY (`offending_user_id`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Report` ADD CONSTRAINT `Report_reporting_user_id_fkey` FOREIGN KEY (`reporting_user_id`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `_friends` ADD CONSTRAINT `_friends_A_fkey` FOREIGN KEY (`A`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `_friends` ADD CONSTRAINT `_friends_B_fkey` FOREIGN KEY (`B`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
