# 🎱 Billiard Game JavaFX

Sebuah game billiard 8-ball klasik yang dibangun menggunakan JavaFX dengan physics engine realistis dan kontrol two-stage aiming system.

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## ✨ Fitur Utama

### 🎮 Gameplay

- **Two-Stage Aiming System**: Klik pertama untuk mengunci sudut, drag untuk mengatur power
- **Realistic Physics Engine**: Sistem tabrakan elastis, friction, dan momentum yang akurat
- **Ball-in-Hand**: Penalti untuk foul dengan sistem drag-and-drop bola putih
- **Visual Aim Line**: Garis bantu prediksi jalur bola dan hasil tabrakan

### 🎯 Sistem Permainan

- Aturan 8-ball standar (Solids vs Stripes)
- Open table di awal permainan
- Sistem deteksi foul:
  - Bola putih masuk lubang
  - Tidak mengenai bola apapun
  - Memasukkan bola lawan
  - Bola 8 masuk sebelum waktunya
- Win condition: Memasukkan semua 7 bola grup + bola 8

### 🎨 Visual

- Meja billiard dengan cushion realistis
- 6 pockets (corner + middle)
- Warna bola standar (1-15 + cue ball)
- HUD dinamis menampilkan:
  - Bola yang sudah masuk per pemain
  - Grup pemain (SOLIDS/STRIPES)
  - Highlight giliran aktif
- Power bar indicator
- Stick visual dengan shadow dan gradient

## 🚀 Cara Menjalankan

### Prerequisites

- Java 17 atau lebih tinggi
- JavaFX SDK 17+

### Instalasi

1. **Clone repository**

```bash
git clone https://github.com/DeoDaniel/BL.git
cd billiard-javafx
```

2. **Compile project**

```bash
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -d out src/billiard/*.java
```

3. **Run game**

```bash
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp out billiard.BilliardGame
```

### Menggunakan IDE (IntelliJ IDEA / Eclipse)

1. Import project sebagai Java project
2. Tambahkan JavaFX library ke project dependencies
3. Set VM options:

```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
```

4. Run `BilliardGame.java`

## 🎮 Kontrol

| Aksi                 | Kontrol                                   |
| -------------------- | ----------------------------------------- |
| **Aiming**           | Gerakkan mouse untuk membidik             |
| **Lock Angle**       | Klik kiri pertama untuk mengunci sudut    |
| **Set Power**        | Drag mundur (menjauhi bola) untuk power   |
| **Shoot**            | Lepas mouse untuk menembak                |
| **Cancel**           | Klik kanan untuk membatalkan lock         |
| **Ball-in-Hand**     | Drag bola putih ke posisi yang diinginkan |
| **Confirm Position** | Klik kiri untuk konfirmasi posisi bola    |

## 📁 Struktur Project

```
billiard/
├── Ball.java              # Class bola dengan physics properties
├── BallGroup.java         # Enum untuk SOLIDS/STRIPES
├── BilliardGame.java      # Main game class & logic
├── Cue.java               # Stick billiard dengan aiming system
├── GamePanel.java         # Canvas panel dengan game loop
├── GameState.java         # Enum state game
├── PhysicsEngine.java     # Engine untuk collision & friction
├── Player.java            # Class pemain
├── Pocket.java            # Class lubang meja
├── Table.java             # Meja billiard dengan rendering
└── module-info.java       # Java module descriptor
```

## 🔧 Konfigurasi Physics

### Constants (PhysicsEngine.java)

```java
RESTITUTION = 0.98        // Elastisitas tabrakan bola (0-1)
WALL_RESTITUTION = 0.95   // Elastisitas pantulan dinding
MIN_SPEED = 0.3           // Threshold kecepatan minimum
```

### Friction System

- Rolling friction: 0.996 (konstan)
- Air drag: Speed-dependent (quadratic)
- Combined friction memberikan deselerasi realistis

## 🎯 Aturan Permainan

### Open Table

- Bola pertama yang masuk menentukan grup pemain
- Pemain yang memasukkan bola pertama mendapat giliran lanjutan

### Giliran Berlanjut Jika:

- Memasukkan bola grup sendiri (tanpa foul)
- Tidak ada foul dalam tembakan

### Giliran Berganti Jika:

- Tidak memasukkan bola apapun
- Terjadi foul (lawan dapat ball-in-hand)

### Win Condition

1. Masukkan semua 7 bola grup sendiri
2. Masukkan bola 8 terakhir
3. Tidak boleh foul saat memasukkan bola 8

### Foul

1. **Cue ball masuk lubang** → Ball-in-hand untuk lawan
2. **No hit** (tidak mengenai bola apapun) → Ball-in-hand
3. **Wrong ball pocketed** (memasukkan bola lawan) → Bola masuk ke lawan
4. **Ball 8 early** (bola 8 masuk sebelum 7 bola selesai) → Dikembalikan ke meja

## 🎨 Customization

### Mengubah Warna Bola

Edit di `Ball.java`, method `render()`:

```java
case 1: mainColor = Color.YELLOW; break;
case 2: mainColor = Color.BLUE; break;
// dst...
```

### Mengubah Ukuran Meja

Edit di `Table.java`:

```java
public final double WIDTH = 1120;
public final double HEIGHT = 560;
```

### Mengubah Power Stick

Edit di `Cue.java`:

```java
public double maxPower = 2500;
```

## 🐛 Known Issues

- Bola kadang bisa "stuck" jika tabrakan terlalu banyak sekaligus
- Ball-in-hand tidak membatasi area kitchen (zona break)
- Tidak ada animasi transisi untuk bola yang masuk

## 🔮 Roadmap

- [ ] Tambah sound effects (tabrakan, masuk lubang)
- [ ] Implementasi spin/english (backspin, sidespin)
- [ ] AI opponent mode
- [ ] Online multiplayer
- [ ] Replay system
- [ ] Tournament mode
- [ ] Customizable table skins
- [ ] Statistics tracking

## 👥 Kontribusi

Kontribusi sangat diterima! Silakan:

1. Fork repository ini
2. Buat branch fitur (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## 📄 License

Project ini dilisensikan di bawah MIT License - lihat file [LICENSE](LICENSE) untuk detail.

## 🙏 Acknowledgments

- Inspirasi dari game 8-ball pool klasik
- JavaFX documentation dan community
- Physics simulation references dari berbagai sumber billiard physics papers

## 📞 Contact

Untuk pertanyaan atau saran, silakan buat issue di repository ini.

---

**Selamat bermain! 🎱**
