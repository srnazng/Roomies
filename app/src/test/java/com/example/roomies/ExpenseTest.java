package com.example.roomies;
import static com.example.roomies.utils.ExpenseUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.view.View;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExpenseTest {
    private List<View> transactionViews;
    private String expenseTotal;

    // success split expense
    @Test
    public void testSplit(){
        transactionViews = new ArrayList<>();
        for(int i=0; i<6; i++){
            transactionViews.add(null);
        }
        expenseTotal = "$0.01";
        boolean success = splitCost(null, expenseTotal, transactionViews, null, 5);
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
    public void testInitCircleExpenses(){
        initCircleExpenses();
        assertNotEquals(null, getCircleExpenses());
    }
}
