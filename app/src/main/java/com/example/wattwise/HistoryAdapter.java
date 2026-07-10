package com.example.wattwise;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends BaseAdapter {

    Activity activity;

    ArrayList<Integer> ids;
    ArrayList<String> months;
    ArrayList<Integer> years;
    ArrayList<Integer> units;
    ArrayList<Integer> rebates;
    ArrayList<Double> costs;

    public HistoryAdapter(
            Activity activity,
            ArrayList<Integer> ids,
            ArrayList<String> months,
            ArrayList<Integer> years,
            ArrayList<Integer> units,
            ArrayList<Integer> rebates,
            ArrayList<Double> costs
    ) {
        this.activity = activity;
        this.ids = ids;
        this.months = months;
        this.years = years;
        this.units = units;
        this.rebates = rebates;
        this.costs = costs;
    }

    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public Object getItem(int position) {
        return ids.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ids.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row;

        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            row = inflater.inflate(R.layout.history_item, parent, false);
        } else {
            row = convertView;
        }

        TextView txtMonthShort = row.findViewById(R.id.txtMonthShort);
        TextView txtMonthYear = row.findViewById(R.id.txtMonthYear);
        TextView txtUsage = row.findViewById(R.id.txtUsage);
        TextView txtAmount = row.findViewById(R.id.txtAmount);

        String month = months.get(position);

        String shortMonth = month.length() >= 3
                ? month.substring(0, 3).toUpperCase()
                : month.toUpperCase();

        txtMonthShort.setText(shortMonth);

        // Requirement: display Month and Final Cost only
        txtMonthYear.setText(month + " " + years.get(position));

        txtUsage.setText("Final Cost");

        txtAmount.setText(
                "RM " + String.format(Locale.US, "%.2f", costs.get(position))
        );

        return row;
    }
}