import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

public class CustomItemModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static int COL_IDX_SELECTED = 0;
    public static int COL_IDX_STATUS = 1;
    public static int COL_IDX_SYSTEM_VALUE = 2;


    private Policy policy;
    private List<CustomItem> items;

    private String[] columns = new String[] {"Select", "Status", "System Value",
            "value_data", "description", "solution", "info", "see_also", "type", "value_type",
            "right_type", "reg_item", "reg_key", "reference"};

    public CustomItemModel(Policy policy) {
        setPolicy(policy);
    }

    @Override
    public int getRowCount() {
        return policy != null ? this.items.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(policy == null) {
            return null;
        }

        CustomItem item = this.items.get(rowIndex);
        switch(columnIndex) {
            case 0:
                return item.isSelected();
            case 1:
                return item.getStatus();
            case 2:
                return item.getSystemValue();
            default:
                String column = columns[columnIndex];
                if(item.getJson().has(column)){
                    String strVal = item.getJson().getString(column);
                    return strVal.replace("\\n", "\n");
                } else {
                    return null;
                }
        }
    }

    public CustomItem getItem(int rowIndex) {
        return this.items.get(rowIndex);
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
        if(policy != null) {
            this.items = policy.getItems();
        }
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex == 0) {
            return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex == 0) {
            CustomItem item = this.items.get(rowIndex);
            item.setSelected((Boolean)aValue);
        }
    }

    public void filter(String searchString) {
        this.items = policy.getItems().stream()
                .filter(item -> {
                    for(String key : item.getJson().keySet()) {
                        if(item.getJson().getString(key).toLowerCase().contains(searchString.toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        fireTableDataChanged();
    }

    public void setStatus(int rowIndex, String status) {
        CustomItem item = this.items.get(rowIndex);
        item.setStatus(status);
        fireTableCellUpdated(rowIndex, COL_IDX_STATUS);
    }

    public void setSystemValue(int rowIndex, String value) {
        CustomItem item = this.items.get(rowIndex);
        item.setSystemValue(value);
        fireTableCellUpdated(rowIndex, COL_IDX_SYSTEM_VALUE);
    }
}
