
# 🎨 SkinHub - Web-Based Skin Manager

**Allow your players to manage their skins via a beautiful, self-hosted web interface. Secure, fast, and offline-mode friendly.**

# 🎨 SkinHub - Web-Based Skin Manager

**Allow your players to manage their skins via a beautiful, self-hosted web interface. Secure, fast, and offline-mode friendly.**

*(Saran: Buat banner sederhana ukuran 1200x300px bertuliskan SkinHub)*

## 👋 Introduction

**SkinHub** revolutionizes how players manage their appearance on your Minecraft server. Instead of struggling with complex commands or being limited by offline-mode restrictions, SkinHub provides a sleek, **built-in web server**.

Players simply generate a secure 6-digit token in-game, login to the web interface, and upload their custom skins or choose from a username. Changes are applied **instantly**\!

## ✨ Key Features

  * **🌐 Integrated Web Interface:** No need for Apache, Nginx, or external hosting. The plugin runs its own lightweight web server (Spark-Java).
  * **🔓 Offline/Cracked Support:** The perfect solution for offline-mode servers. Skins are stored locally and restored automatically when players join.
  * **🔒 Token-Based Security:** **No passwords required.** Players login using a temporary, time-sensitive 6-digit token generated in-game. Secure and simple.
  * **🖼️ 3D Skin Preview:** Players can rotate and view their skin model (Steve/Alex) in real-time on the browser before applying.
  * **⚡ Real-Time Updates:** Skins update instantly for online players without needing to reconnect.
  * **📁 Multiple Sources:** Support for:
      * Uploading local `.png` files.
      * Importing from URL.
      * Stealing/Copying from valid Minecraft Usernames.
  * **🛡️ HTTPS Ready:** Supports SSL via flexible KeyStore configuration or Reverse Proxy.

-----

## 📸 Screenshots

*(Di sini Anda wajib mengupload screenshot tampilan web plugin Anda)*

| Login Page | Skin Manager & 3D Preview |
| :---: | :---: |
|  |  |

-----

## 🎮 How It Works (User Guide)

1.  **Get the Token:** Type `/skinhub link` in-game. You will receive a secret 6-digit code (e.g., `829103`).
2.  **Login:** Click the link provided or go to the server's web address (e.g., `https://skin.yourserver.com`). Enter your code.
3.  **Manage:** Upload your `.png` or type a username.
4.  **Apply:** Click save, and watch your skin change in-game instantly\!

-----

## 🛠️ Installation & Setup

1.  Download the `SkinHub.jar`.
2.  Drop it into your server's `/plugins/` folder.
3.  Restart your server.
4.  **Port Forwarding:** Make sure the port defined in `config.yml` (Default: `25566`) is open and forwarded so players can access the website.

### 🔐 SSL / HTTPS Setup (Optional but Recommended)

SkinHub supports two ways to secure your web panel:

1.  **Reverse Proxy (Recommended):** Use Nginx or Caddy to proxy traffic to the plugin's port. This handles SSL automatically.
2.  **Built-in SSL:**
      * Generate a PKCS12 KeyStore (`.p12`) containing your certificate.
      * Place it in the SkinHub plugin folder.
      * Enable SSL in `config.yml` and provide the password.

-----

## ⚙️ Configuration

Everything is customizable in `config.yml`:

```yaml
web-server:
  port: 25566
  # URL shown to players when they type /skinhub link
  public-url: "http://your-server-ip:25566"

ssl:
  enabled: false
  keystore-path: "keystore.p12"
  keystore-password: "change-me"

security:
  token-expiry-seconds: 120 # Token valid for 2 minutes
  session-expiry-days: 7    # Web login lasts for 7 days
```

-----

## 📜 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/skinhub link` | `skinhub.use` | Generates a login token for the web interface. |
| `/skinhub reload` | `skinhub.admin` | Reloads the configuration and restarts the web server. |

-----

## 💻 Compatibility

  * **Java:** Java 21 or higher.
  * **Server Software:** Paper, Purpur, or Folia (1.21.x+).
  * **Browser:** Works on all modern browsers (Chrome, Firefox, Safari, Edge) and Mobile devices.

-----

## 🤝 Support & Source

Found a bug? Have a suggestion?

  * [**Issue Tracker**](https://www.google.com/search?q=LINK_REPOSITORY_GITHUB_ANDA)
  * [**Source Code**](https://www.google.com/search?q=LINK_REPOSITORY_GITHUB_ANDA)
  * [**Discord Support**](https://www.google.com/search?q=LINK_DISCORD_JIKA_ADA)

## 👋 Introduction

**SkinHub** revolutionizes how players manage their appearance on your Minecraft server. Instead of struggling with complex commands or being limited by offline-mode restrictions, SkinHub provides a sleek, **built-in web server**.

Players simply generate a secure 6-digit token in-game, login to the web interface, and upload their custom skins or choose from a username. Changes are applied **instantly**\!

## ✨ Key Features

  * **🌐 Integrated Web Interface:** No need for Apache, Nginx, or external hosting. The plugin runs its own lightweight web server (Spark-Java).
  * **🔓 Offline/Cracked Support:** The perfect solution for offline-mode servers. Skins are stored locally and restored automatically when players join.
  * **🔒 Token-Based Security:** **No passwords required.** Players login using a temporary, time-sensitive 6-digit token generated in-game. Secure and simple.
  * **🖼️ 3D Skin Preview:** Players can rotate and view their skin model (Steve/Alex) in real-time on the browser before applying.
  * **⚡ Real-Time Updates:** Skins update instantly for online players without needing to reconnect.
  * **📁 Multiple Sources:** Support for:
      * Uploading local `.png` files.
      * Importing from URL.
      * Stealing/Copying from valid Minecraft Usernames.
  * **🛡️ HTTPS Ready:** Supports SSL via flexible KeyStore configuration or Reverse Proxy.

-----

## 📸 Screenshots

*(Di sini Anda wajib mengupload screenshot tampilan web plugin Anda)*

| Login Page | Skin Manager & 3D Preview |
| :---: | :---: |
|  |  |

-----

## 🎮 How It Works (User Guide)

1.  **Get the Token:** Type `/skinhub link` in-game. You will receive a secret 6-digit code (e.g., `829103`).
2.  **Login:** Click the link provided or go to the server's web address (e.g., `https://skin.yourserver.com`). Enter your code.
3.  **Manage:** Upload your `.png` or type a username.
4.  **Apply:** Click save, and watch your skin change in-game instantly\!

-----

## 🛠️ Installation & Setup

1.  Download the `SkinHub.jar`.
2.  Drop it into your server's `/plugins/` folder.
3.  Restart your server.
4.  **Port Forwarding:** Make sure the port defined in `config.yml` (Default: `25566`) is open and forwarded so players can access the website.

### 🔐 SSL / HTTPS Setup (Optional but Recommended)

SkinHub supports two ways to secure your web panel:

1.  **Reverse Proxy (Recommended):** Use Nginx or Caddy to proxy traffic to the plugin's port. This handles SSL automatically.
2.  **Built-in SSL:**
      * Generate a PKCS12 KeyStore (`.p12`) containing your certificate.
      * Place it in the SkinHub plugin folder.
      * Enable SSL in `config.yml` and provide the password.

-----

## ⚙️ Configuration

Everything is customizable in `config.yml`:

```yaml
web-server:
  port: 25566
  # URL shown to players when they type /skinhub link
  public-url: "http://your-server-ip:25566"

ssl:
  enabled: false
  keystore-path: "keystore.p12"
  keystore-password: "change-me"

security:
  token-expiry-seconds: 120 # Token valid for 2 minutes
  session-expiry-days: 7    # Web login lasts for 7 days
```

-----

## 📜 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/skinhub link` | `skinhub.use` | Generates a login token for the web interface. |
| `/skinhub reload` | `skinhub.admin` | Reloads the configuration and restarts the web server. |

-----

## 💻 Compatibility

  * **Java:** Java 21 or higher.
  * **Server Software:** Paper, Purpur, or Folia (1.21.x+).
  * **Browser:** Works on all modern browsers (Chrome, Firefox, Safari, Edge) and Mobile devices.

-----

## 🤝 Support & Source

Found a bug? Have a suggestion?

  * [**Issue Tracker**](https://www.google.com/search?q=LINK_REPOSITORY_GITHUB_ANDA)
  * [**Source Code**](https://www.google.com/search?q=LINK_REPOSITORY_GITHUB_ANDA)
  * [**Discord Support**](https://www.google.com/search?q=LINK_DISCORD_JIKA_ADA)
