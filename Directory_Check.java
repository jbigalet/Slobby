package tvshows_renamer;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Directory_Check extends javax.swing.JPanel {

    DefaultTableModel model = new DefaultTableModel();
    String[] tvshows;
    public Directory_Check() throws Exception {
        initComponents();
        model.addColumn("File");
        model.addColumn("TVShow");
        model.addColumn("Score");
        model.addColumn("Renamed");
        tvshows = Main.GetTopTVShowsList(10000);
        jTable.setDefaultRenderer(Object.class, new MyTableCellRender());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton_Directory = new javax.swing.JButton();
        jTextField_Directory = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        jButton_Check = new javax.swing.JButton();
        jProgressBar_Checking = new javax.swing.JProgressBar();

        jButton_Directory.setText("Open Directory");
        jButton_Directory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_DirectoryActionPerformed(evt);
            }
        });

        jTable.setModel(model);
        jTable.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(jTable);
        jTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jButton_Check.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton_Check.setText("CHECK");
        jButton_Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_Check, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                    .addComponent(jProgressBar_Checking, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton_Directory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField_Directory, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_Directory)
                    .addComponent(jTextField_Directory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_Check, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar_Checking, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    final JFileChooser JFC_Directory = new JFileChooser();

    private void jButton_DirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_DirectoryActionPerformed
        JFC_Directory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = JFC_Directory.showOpenDialog(null);
        if(ret != JFileChooser.APPROVE_OPTION)
            return ;

        File dir = JFC_Directory.getSelectedFile();
        jTextField_Directory.setText(dir.getAbsolutePath());

        int nColumn = model.getColumnCount();
        String[] filelist = dir.list();
        for(String file : filelist){
            Object[] tmp = new Object[nColumn];
            tmp[0] = file;
            model.addRow(tmp);
        }
    }//GEN-LAST:event_jButton_DirectoryActionPerformed

    private void jButton_CheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CheckActionPerformed
        try{
            int nRow = model.getRowCount();
            jProgressBar_Checking.setMinimum(0);
            jProgressBar_Checking.setMaximum(nRow);
            for(int i=0 ; i<nRow ; i++){
                String file = (String)jTable.getValueAt(i, 0);
                Object[] tmp = FileTest(file);
                jTable.getModel().setValueAt((String)tmp[0], i, 1);
                jTable.getModel().setValueAt((Double)tmp[2], i, 2);
                jTable.getModel().setValueAt((String)tmp[1], i, 3);
                jProgressBar_Checking.setValue(i);
            }
        } catch (Exception e) {System.out.println(e.toString());}
    }//GEN-LAST:event_jButton_CheckActionPerformed


    public Object[] FileTest(String tfile) throws Exception {
        //String file = Levenshtein.CorrectFileName(tfile.substring(0,tfile.lastIndexOf(".")));
        String file = tfile;
        double minScore = Integer.MAX_VALUE;
        String bestShow = "";
        for(String show : tvshows){
            double score = Levenshtein.Distance(file, show);
            if(score < minScore){
                minScore = score;
                bestShow = show;
            }
        }
        return new Object[] {bestShow, file, minScore};
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Check;
    private javax.swing.JButton jButton_Directory;
    private javax.swing.JProgressBar jProgressBar_Checking;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable;
    private javax.swing.JTextField jTextField_Directory;
    // End of variables declaration//GEN-END:variables

}




class MyTableCellRender extends DefaultTableCellRenderer {

    public MyTableCellRender() {
        super();
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(table.getValueAt(row, 2) != null){
            if ((Double)table.getValueAt(row, 2) > 1) {
                setForeground(Color.black);
                setBackground(Color.red);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }
        }
        setText(value != null ? value.toString() : "");
        return this;
    }
}
