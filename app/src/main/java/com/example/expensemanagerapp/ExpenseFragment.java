package com.example.expensemanagerapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import Model.Data;

//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link ExpenseFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class ExpenseFragment extends Fragment {
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public ExpenseFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment ExpenseFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static ExpenseFragment newInstance(String param1, String param2) {
//        ExpenseFragment fragment = new ExpenseFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }


    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    private RecyclerView recyclerView;

    private TextView expenseSumResult;

    private EditText edtAmount, edtType, edtNote;
    private Button btnUpdate, btnDelete;

    private String type, note;
    private int amount;
    private String post_key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_expense, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseDatabase").child(uid);
        Log.d("BBB", mExpenseDatabase + "");

        expenseSumResult = myView.findViewById(R.id.expense_txt_result);
        recyclerView = myView.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Tính tổng các khoản chi
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int expenseSum = 0;
                for (DataSnapshot mySnapshot : snapshot.getChildren()) {
                    Data data = mySnapshot.getValue(Data.class);
                    expenseSum += data.getAmount();

                    String strExpenseSum = String.valueOf(expenseSum);

                    expenseSumResult.setText(strExpenseSum+".00");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //khoi tao firebaserecycleroption thi moi co firebaserecyclerAdapter
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mExpenseDatabase, Data.class)
                .setLifecycleOwner(this)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Data model) {
                holder.setDate(model.getDate());
                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setAmount(model.getAmount());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post_key = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        updateDataItem();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler_data, parent, false));
            }
        };
        recyclerView.setAdapter(adapter);
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        private void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }

        private void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }

        private void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);
        }

        private void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_txt_expense);
            String stAmount = String.valueOf(amount);
            mAmount.setText(stAmount);
        }
    }

    private void updateDataItem() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View myView = inflater.inflate(R.layout.update_data_item, null);
        mydialog.setView(myView);

        edtAmount = myView.findViewById(R.id.udamount_edt);
        edtType = myView.findViewById(R.id.udtype_edt);
        edtNote = myView.findViewById(R.id.udnote_edt);

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        btnUpdate = myView.findViewById(R.id.btnupdate);
        btnDelete = myView.findViewById(R.id.btn_delete);

        final AlertDialog dialog = mydialog.create();

        dialog.setCancelable(false);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                String stAmount = String.valueOf(amount);
                stAmount = edtAmount.getText().toString().trim();

                int intAmount = Integer.parseInt(stAmount);

                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(intAmount,type,note,post_key,mDate);

                mExpenseDatabase.child(post_key).setValue(data);

                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mExpenseDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
