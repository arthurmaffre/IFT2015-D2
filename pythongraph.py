import pandas as pd
import matplotlib
matplotlib.use("Agg")           # pas de fenêtre
import matplotlib.pyplot as plt
from pathlib import Path

CSV_FILE = "sim.csv"
OUT_DIR  = Path("figures"); OUT_DIR.mkdir(exist_ok=True)

POP_H, COAL_H = "time,population", "time,paternal"

# ---------- lecture & découpe --------------------------------------------
with open(CSV_FILE, encoding="utf-8") as f:
    lines = f.readlines()
sep = next(i for i, ln in enumerate(lines) if ln.startswith(COAL_H))
pop_df  = pd.read_csv(pd.io.common.StringIO("".join(lines[:sep])))
coal_df = pd.read_csv(pd.io.common.StringIO("".join(lines[sep:])))

# inversion temporelle pour lignées (on garde les années brutes)
t_max = coal_df["time"].max()
coal_df["time_inv"] = t_max - coal_df["time"]

# ---------- figure combinée ----------------------------------------------
plt.figure(figsize=(10, 6))

ax_pop = plt.gca()
ax_pop.plot(pop_df["time"], pop_df["population"],
            color="tab:blue", lw=2, label="taille de la population")
ax_pop.set_ylabel("Population", color="tab:blue", fontweight="bold")
ax_pop.tick_params(axis="y", labelcolor="tab:blue")
ax_pop.grid(alpha=0.25)

# graduations X exactement sur les temps de la 1ʳᵉ colonne
ax_pop.set_xticks(pop_df["time"])
ax_pop.set_xlabel("Temps (années)", fontweight="bold")

ax_coal = ax_pop.twinx()
ax_coal.plot(coal_df["time_inv"], coal_df["maternal"],
             color="maroon", marker="s", ms=4, lw=1.2, label="aïeules")
ax_coal.plot(coal_df["time_inv"], coal_df["paternal"],
             color="forestgreen", marker="o", ms=3, lw=1.2, label="aïeux")
ax_coal.set_yscale("log")
ax_coal.invert_yaxis()
ax_coal.set_ylabel("Nombre de lignées (log10)", fontweight="bold")

# légende fusionnée
handles, labels = [], []
for ax in (ax_pop, ax_coal):
    h, l = ax.get_legend_handles_labels()
    handles += h; labels += l
ax_coal.legend(handles, labels, frameon=False, loc="upper right")

plt.title("Évolution population & coalescence", pad=12)
plt.tight_layout()

out = OUT_DIR / "coalescence_combo.png"
plt.savefig(out, dpi=300)
plt.close()
print(f"✅  Figure enregistrée : {out}")