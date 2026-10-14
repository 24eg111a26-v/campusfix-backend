require("dotenv").config();
const express = require("express");
const cors = require("cors");
const path = require("path");
const fs = require("fs");
const mongoose = require("mongoose");

const authRoutes = require("./routes/auth");
const complaintRoutes = require("./routes/complaints");

const app = express();
const UPLOAD_DIR = path.join(__dirname, "uploads");
if (!fs.existsSync(UPLOAD_DIR)) fs.mkdirSync(UPLOAD_DIR);

app.use(cors({ origin: process.env.CLIENT_URL || "*" }));
app.use(express.json());
app.use("/uploads", express.static(UPLOAD_DIR));

app.get("/", (req, res) => res.json({ message: "Campus Fix API is running" }));
app.use("/api/auth", authRoutes);
app.use("/api/complaints", complaintRoutes);

// Basic error handler (also catches upload errors)
app.use((err, req, res, next) => {
  console.error(err.message);
  res.status(400).json({ message: err.message || "Something went wrong" });
});

const PORT = process.env.PORT || 5000;
mongoose
  .connect(process.env.MONGO_URI)
  .then(() => {
    console.log("MongoDB connected");
    app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
  })
  .catch((err) => {
    console.error("MongoDB connection failed:", err.message);
    process.exit(1);
  });
