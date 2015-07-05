//package com.mycheez.adapter;
//
//import java.util.ArrayList;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.Resources;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import com.mycheez.R;
//import com.mycheez.viewmodel.HistoryViewModel;
//
//public class HistoryListAdapter extends BaseAdapter implements OnClickListener {
//
//    /*********** Declare Used Variables *********/
//    private Activity activity;
//    private ArrayList<HistoryViewModel> data;
//    private static LayoutInflater inflater;
//    private Resources res;
//    private HistoryViewModel tempValues;
//    int i=0;
//
//    /*************  CustomAdapter Constructor *****************/
//    public HistoryListAdapter(Activity a, ArrayList<HistoryViewModel> d,Resources resLocal) {
//
//           /********** Take passed values **********/
//            activity = a;
//            data=d;
//            res = resLocal;
//
//            /***********  Layout inflator to call external xml layout () ***********/
//             inflater = ( LayoutInflater )activity.
//                                         getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//    }
//
//    /******** What is the size of Passed Arraylist Size ************/
//    public int getCount() {
//
//        if(data.size()<=0)
//            return 1;
//        return data.size();
//    }
//
//    public Object getItem(int position) {
//        return position;
//    }
//
//    public long getItemId(int position) {
//        return position;
//    }
//
//    /********* Create a holder Class to contain inflated xml file elements *********/
//    public static class ViewHolder{
//
//        public TextView friendNameTextView;
//        public TextView stoleCheeseTextView;
//    }
//
//    /****** Depends upon data size called for each row , Create each ListView row *****/
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        View vi = convertView;
//        ViewHolder holder;
//
//        if(convertView==null){
//
//            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
//            vi = inflater.inflate(R.layout.history_row, null);
//
//            /****** View Holder Object to contain tabitem.xml file elements ******/
//
//            holder = new ViewHolder();
//            holder.friendNameTextView = (TextView) vi.findViewById(R.id.friendNameTextview);
//            holder.stoleCheeseTextView=(TextView)vi.findViewById(R.id.stoleCheeseTextView);
//
//           /************  Set holder with LayoutInflater ************/
//            vi.setTag( holder );
//        }
//        else
//            holder=(ViewHolder)vi.getTag();
//
//        if(data.size() > 0)
//        {
//            /***** Get each Model object from Arraylist ********/
//            tempValues=null;
//            tempValues = ( HistoryViewModel ) data.get( position );
//
//            /************  Set Model values in Holder elements ***********/
//
//             holder.friendNameTextView.setText( tempValues.getFriendName());
//             //use Picasso to load image into ImageView
//
//             /******** Set Item Click Listner for LayoutInflater for each row *******/
//
//             vi.setOnClickListener(new OnItemClickListener( position ));
//        }
//        return vi;
//    }
//
//    @Override
//    public void onClick(View v) {
//
//    }
//
//    /********* Called when Item click in ListView ************/
//    private class OnItemClickListener  implements OnClickListener{
//        private int mPosition;
//
//        OnItemClickListener(int position){
//             mPosition = position;
//        }
//
//        @Override
//        public void onClick(View arg0) {
//
//
//          //MainActivity sct = (MainActivity)activity;
//
//         /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
//
//            //sct.onItemClick(mPosition);
//        }
//    }
//}
