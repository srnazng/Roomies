package com.example.roomies;
import static com.example.roomies.utils.ExpenseUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.view.View;

import com.example.roomies.model.ExpenseCollection;
import com.parse.ParseException;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExpenseTest {
    private List<View> transactionViews;
    private String expenseTotal;
    private ExpenseCollection expenseCollection;

    public static final int NUM_ITEMS = 5;

    @Before
    public void init(){
        expenseCollection = new ExpenseCollection();
    }

    // success split expense
    @Test
    public void testSplit(){
        transactionViews = new ArrayList<>();
        for(int i=0; i<NUM_ITEMS; i++){
            transactionViews.add(null);
        }
        expenseTotal = "$0.01";
        boolean success = splitCost(null, expenseTotal, transactionViews, null, NUM_ITEMS);
        assertEquals(true, success);
        assertEquals("0.00", findAssignedSum(null, transactionViews));
    }

    // split expense with no one assigned transaction
    @Test
    public void testSplitWithNoAssigned(){
        transactionViews = new ArrayList<>();
        expenseTotal = "$0.01";
        boolean success = splitCost(null, expenseTotal, transactionViews, null, 0);
        assertEquals(false, success);
        assertEquals("0.00", findAssignedSum(null, transactionViews));
    }

    // split empty expense
    @Test
    public void testSplitWithEmptyExpense(){
        expenseTotal = "$";
        boolean success = splitCost(null, expenseTotal, transactionViews, null, 5);
        assertEquals(false, success);
    }

    // split when expense is 0.00
    @Test
    public void testSplitWithNoExpense(){
        expenseTotal = "$0.00";
        boolean success = splitCost(null, expenseTotal, transactionViews, null, 5);
        assertEquals(false, success);
    }

    // remove $ and , from string
    @Test
    public void testRemoveDollar(){
        String price = "$3,000,000.00";
        assertEquals("3000000.00", removeDollar(price));
    }

    // init circle expenses
    @Test
    public void testInitCircleExpenses() throws ParseException {
        expenseCollection.initCircleExpenses(null);
        assertNotEquals(null, expenseCollection.getCircleExpenses());
    }

    // init circle expenses
    @Test
    public void testInitCircleTransactions(){
        expenseCollection.initCircleTransactions(null);
        assertNotEquals(null, expenseCollection.getCircleTransactions());
        assertNotEquals(null, expenseCollection.getMyCompletedPayments());
        assertNotEquals(null, expenseCollection.getMyPendingPayments());
        assertNotEquals(null, expenseCollection.getMyCompletedRequests());
        assertNotEquals(null, expenseCollection.getMyPendingRequests());
    }

}
