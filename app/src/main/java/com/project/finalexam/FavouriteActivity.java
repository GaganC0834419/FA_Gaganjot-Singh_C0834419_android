package com.project.finalexam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.finalexam.db.AppDatabase;
import com.project.finalexam.db.DatabaseClient;
import com.project.finalexam.db.entities.AddExpense;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavouriteActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    List<AddExpense> ledgers;
    ArrayList<AddExpense> newList;
    LedgerAdapter ledgerAdapter;

    @BindView(R.id.noData_ll)
    LinearLayout noDataLl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        ButterKnife.bind(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(FavouriteActivity.this));

        getSavedPlaces();
    }

    private void getSavedPlaces() {
        class GetLedgerHistory extends AsyncTask<Void, Void, List<AddExpense>> {

            @Override
            protected List<AddExpense> doInBackground(Void... voids) {
                ledgers = DatabaseClient
                        .getInstance(FavouriteActivity.this)
                        .getAppDatabase()
                        .addExpenseDao()
                        .getAll();
                return ledgers;
            }

            @Override
            protected void onPostExecute(List<AddExpense> ledgers) {
                super.onPostExecute(ledgers);


                newList = new ArrayList<>(ledgers.size());

                if (ledgers.size() == 0) {
                    noDataLl.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);


                } else {

                    mRecyclerView.setVisibility(View.VISIBLE);
                    noDataLl.setVisibility(View.GONE);
                    ledgerAdapter = new LedgerAdapter(FavouriteActivity.this, ledgers);
                    Log.e("List", ledgers + "");
                    mRecyclerView.setAdapter(ledgerAdapter);
                }


            }
        }
        GetLedgerHistory getLedgerHistory = new GetLedgerHistory();
        getLedgerHistory.execute();
    }

    private void deleteSelectedRow(AddExpense model) {
        class DeleteSelectedRow extends AsyncTask<Void, Void, Void> {

            boolean isSuccess = false;

            @Override
            protected Void doInBackground(Void... voids) {

                final AppDatabase appDatabase = DatabaseClient.getInstance(FavouriteActivity.this).getAppDatabase();
                appDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.addExpenseDao().delete(model);

                    }
                });


                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getSavedPlaces();
            }
        }

        DeleteSelectedRow deleteSelectedRow = new DeleteSelectedRow();
        deleteSelectedRow.execute();
    }


    //---------------------------Adapter------------------------------------//
    public class LedgerAdapter extends RecyclerView.Adapter<LedgerAdapter.MyViewHolder> {

        Context context;
        List<AddExpense> childFeedList;


        public LedgerAdapter(Context context, List<AddExpense> childFeedList) {
            this.context = context;
            this.childFeedList = childFeedList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_list_design, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final AddExpense ledger = childFeedList.get(position);
            holder.mAccountNameTv.setText(ledger.getName());
            if(ledger.getIsVisited().equalsIgnoreCase("false")){
                holder.mVisited.setText("Mark As Visited");
                holder.mCardView.setBackgroundColor(Color.parseColor("#FFC4C0"));
            }else{
                holder.mVisited.setText("Already Visited");
                holder.mCardView.setBackgroundColor(Color.parseColor("#F1FFF3"));
            }


            holder.mDeleteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteSelectedRow(ledger);

                }
            });

            holder.mEditTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //setEditDialog(ledger);

                }
            });

            holder.mFrontLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(FavouriteActivity.this,FavMapsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("OBJ", ledger);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            });

            holder.mVisited.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateVisitStatus(ledger);
                }
            });


        }




        @Override
        public int getItemCount() {
            return childFeedList.size();
        }



        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView mAccountNameTv,mVisited;
            TextView mDeleteTv;
            TextView mEditTv;
            View mFrontLayout;
            CardView mCardView;


            public MyViewHolder(View itemView) {
                super(itemView);

                mAccountNameTv = (TextView) itemView.findViewById(R.id.accountName_tv);
                mVisited = (TextView) itemView.findViewById(R.id.mVisited);
                mDeleteTv = (TextView) itemView.findViewById(R.id.delete_tv);
                mEditTv = (TextView) itemView.findViewById(R.id.edit_tv);
                mFrontLayout = (FrameLayout) itemView.findViewById(R.id.frontLayout);
                mCardView = itemView.findViewById(R.id.mCardView);


            }
        }
    }

    private void updateVisitStatus(AddExpense ledger) {
        class SaveExpense extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                ledger.setIsVisited("true");

                //adding to database
                DatabaseClient.getInstance(FavouriteActivity.this).getAppDatabase()
                        .addExpenseDao()
                        .update(ledger);
                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(FavouriteActivity.this, "Status Changed", Toast.LENGTH_LONG).show();
                getSavedPlaces();
            }
        }

        SaveExpense saveExpense = new SaveExpense();
        saveExpense.execute();
    }
}