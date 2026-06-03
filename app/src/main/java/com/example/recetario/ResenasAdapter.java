package com.example.recetario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ResenasAdapter extends RecyclerView.Adapter<ResenasAdapter.ResenaViewHolder> {

    private final List<Resenia.ReseniaResponseDto> listaResenas;

    public ResenasAdapter(List<Resenia.ReseniaResponseDto> listaResenas) {
        this.listaResenas = listaResenas;
    }

    @NonNull
    @Override
    public ResenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resenia, parent, false);
        return new ResenaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ResenaViewHolder holder, int position) {
        Resenia.ReseniaResponseDto resenia = listaResenas.get(position);

        // Si tienes nombres reales mapeados, los usas; si no, mostramos el ID recortado de Mongo
        if (resenia.getUsuarioId() != null && resenia.getUsuarioId().length() > 6) {
            holder.txtUsuario.setText("👤 Colaborador (..." + resenia.getUsuarioId().substring(resenia.getUsuarioId().length() - 5) + ")");
        } else {
            holder.txtUsuario.setText("👤 Anónimo");
        }

        // Formatear el puntaje numérico en estrellas visuales
        int estrellas = resenia.getPuntaje() != null ? resenia.getPuntaje() : 0;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < estrellas; i++) stars.append("⭐");
        holder.txtPuntaje.setText(stars.toString());

        holder.txtComentario.setText(resenia.getComentario());

        // Procesar y recortar la fecha ISO que viene del backend (ej: 2026-06-03T15:00:00 -> 2026-06-03)
        if (resenia.getFechaCreacion() != null && resenia.getFechaCreacion().contains("T")) {
            String fechaCorta = resenia.getFechaCreacion().split("T")[0];
            holder.txtFecha.setText(fechaCorta);
        } else {
            holder.txtFecha.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return listaResenas != null ? listaResenas.size() : 0;
    }

    public static class ResenaViewHolder extends RecyclerView.ViewHolder {
        TextView txtUsuario, txtFecha, txtPuntaje, txtComentario;

        public ResenaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUsuario = itemView.findViewById(R.id.txtItemUsuario);
            txtFecha = itemView.findViewById(R.id.txtItemFecha);
            txtPuntaje = itemView.findViewById(R.id.txtItemPuntaje);
            txtComentario = itemView.findViewById(R.id.txtItemComentario);
        }
    }
}