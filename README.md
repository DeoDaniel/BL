# 🎱 Billiard Game JavaFX

Sebuah game billiard 8-ball klasik yang dibangun menggunakan JavaFX dengan physics engine realistis dan kontrol two-stage aiming system.

✨ Fitur Utama
🎮 Gameplay

Two-Stage Aiming System: Klik pertama untuk mengunci sudut, drag untuk mengatur power
Realistic Physics Engine: Sistem tabrakan elastis, friction, dan momentum yang akurat
Ball-in-Hand: Penalti untuk foul dengan sistem drag-and-drop bola putih
Visual Aim Line: Garis bantu prediksi jalur bola dan hasil tabrakan

🎯 Sistem Permainan

Aturan 8-ball standar (Solids vs Stripes)
Open table di awal permainan
Sistem deteksi foul:

Bola putih masuk lubang
Tidak mengenai bola apapun
Memasukkan bola lawan
Bola 8 masuk sebelum waktunya

Win condition: Memasukkan semua 7 bola grup + bola 8

🎨 Visual

Meja billiard dengan cushion realistis
6 pockets (corner + middle)
Warna bola standar (1-15 + cue ball)
HUD dinamis menampilkan:

Bola yang sudah masuk per pemain
Grup pemain (SOLIDS/STRIPES)
Highlight giliran aktif

Power bar indicator
Stick visual dengan shadow dan gradient

🚀 Cara Menjalankan
Prerequisites

Java 17 atau lebih tinggi
JavaFX SDK 17+
