import javafx.application.Application;  //Application class
import javafx.stage.Stage;  //Stage for application window
import javafx.scene.Scene;  //Scene for application window
import javafx.scene.control.*;  //UI controls
import javafx.scene.control.TextInputDialog;        //TextInputDialog for application window
import javafx.scene.control.Alert;      //Alert control
import javafx.scene.layout.GridPane;    //Grid layout
import javafx.scene.layout.VBox;    //Vertical layout
import javafx.beans.property.SimpleStringProperty;  
import javafx.collections.FXCollections;    //FXCollections for Observablelist
import javafx.collections.ObservableList;   //for data source/binding
import javafx.geometry.Pos;     //Position for layout alignment 
import javafx.geometry.Insets;  //Insets for layout padding
import javafx.stage.Popup;
import java.util.*;     //Map, Set, List


public class AlarmScheduleGUI extends Application{

    private Map<String, String> alarmQualifications = new HashMap<>();      //Store alarm qualifications
    private Map<String, Set<String>> expertQualifications = new HashMap<>();        //Store expert qualifications
    private TableView<ObservableList<String>> tableView;    //table display
    private Map<String, Set<String>> schedule;      //Store schedule data
    //expert names
    private ObservableList<String> expertNames = FXCollections.observableArrayList("e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8");
    private boolean isEditable = false;
    private Button saveButton;
    private Set<String> activeAlarms = new HashSet<>(); //To store active alarms
    private Map<String, Integer> expertIDs = new HashMap<>();   //To store expert IDs

    //Expert ID initialization
    private void initializeExpertIDs() {
        expertIDs.put("e1", 134);
        expertIDs.put("e2", 145);
        expertIDs.put("e3", 154);
        expertIDs.put("e4", 165);
        expertIDs.put("e5", 169);
        expertIDs.put("e6", 174);
        expertIDs.put("e7", 181);
        expertIDs.put("e8", 190);
    }

    //Initialize qualifications
    private void initializeQualifications() {
        alarmQualifications.put("Power supply missing", "Elec");
        alarmQualifications.put("Tank overflow", "Mech");
        alarmQualifications.put("CO2 detected", "Chem");
        alarmQualifications.put("Biological attack", "Bio");
        expertQualifications.put("e1", new HashSet<>(Arrays.asList("Elec")));
        expertQualifications.put("e2", new HashSet<>(Arrays.asList("Mech", "Chem")));
        expertQualifications.put("e3", new HashSet<>(Arrays.asList("Bio", "Chem", "Elec")));
        expertQualifications.put("e4", new HashSet<>(Arrays.asList("Bio")));
        expertQualifications.put("e5", new HashSet<>(Arrays.asList("Chem", "Bio")));
        expertQualifications.put("e6", new HashSet<>(Arrays.asList("Elec", "Mech", "Chem", "Bio")));
        expertQualifications.put("e7", new HashSet<>(Arrays.asList("Mech","Elec")));
        expertQualifications.put("e8", new HashSet<>(Arrays.asList("Mech", "Bio")));
    }

     //Initialize schedule
     private void initializeSchedule() {
        schedule = new LinkedHashMap<>();
        schedule.put("Monday day", new HashSet<>(Arrays.asList("e7","e5","e1")));
        schedule.put("Monday night", new HashSet<>(Arrays.asList("e6")));
        schedule.put("Tuesday day", new HashSet<>(Arrays.asList("e1","e3","e8")));    
        schedule.put("Tuesday night", new HashSet<>(Arrays.asList("e6")));
        schedule.put("Wednesday day", new HashSet<>());
        System.out.println("Initialized Schedule:"  + schedule);
    }

    //start method to set up UI
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chemical Plant Alarm System");   //Window Title

        initializeQualifications(); //Qualifications initialization
        initializeSchedule();       //Schedule initialization
        initializeExpertIDs();      //ExpertID initialization
        tableView = new TableView<>();  //create table
        setupTable();       //Set up table

        //Period Section
        VBox periodSection = new VBox(10);
        periodSection.setAlignment(Pos.TOP_LEFT);
        Label periodLabel = new Label("Manage Periods");
        periodSection.getChildren().add(periodLabel);

        TextField addPeriodField = new TextField();
        addPeriodField.setPromptText("Enter period Name");

        Button addPeriodButton = new Button("Add Period");
        addPeriodButton.setOnAction(e -> addPeriod(addPeriodField.getText()));

        Button removePeriodButton = new Button("Remove Period");
        removePeriodButton.setOnAction(e -> removePeriod(addPeriodField.getText()));

        periodSection.getChildren().addAll(addPeriodField, addPeriodButton, removePeriodButton);

        //Alarm Section
        VBox activeAlarmsSection = new VBox(10);
        activeAlarmsSection.setAlignment(Pos.TOP_LEFT);
        Label activeAlarmsLabel = new Label("Activate Alarms");
        activeAlarmsSection.getChildren().add(activeAlarmsLabel);

        CheckBox powerSupplyCheck = new CheckBox("Power supply missing");
        CheckBox tankOverflowCheck = new CheckBox("Tank overflow");
        CheckBox co2DetectedCheck = new CheckBox("CO2 detected");
        CheckBox biologicalAttackCheck = new CheckBox("Biological attack");

        powerSupplyCheck.setSelected(true);
        tankOverflowCheck.setSelected(true);
        co2DetectedCheck.setSelected(true);

        activeAlarmsSection.getChildren().addAll(powerSupplyCheck, tankOverflowCheck, co2DetectedCheck, biologicalAttackCheck);

        Button numExpertsButton = new Button("Number of Experts");
        numExpertsButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter Period");
            dialog.setHeaderText("Enter Period (e.g., Monday day) :");
            dialog.setContentText("Period:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(period -> {
                int numberOfExperts = getNumberOfExperts(period);

                System.out.println("Number of Experts for" + period + ":" + numberOfExperts);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Expert Count is");
                alert.setHeaderText(null);
                alert.setContentText("Number of Experts for " + period + ": " + numberOfExperts);
                alert.showAndWait();
            });
        });

        Button editScheduleButton = new Button("Edit Schedule");
        editScheduleButton.setOnAction(e -> toggleEditSchedule());

        Button expertsOnDutyButton = new Button("Experts on Duty");
        expertsOnDutyButton.setOnAction(e -> showExpertsOnDuty());

        Button expertOnPageButton = new Button("Expert on Page");
        expertOnPageButton.setOnAction(e -> showExpertOnPage());

        saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveSchedule());
        saveButton.setDisable(true);

        //Layout for GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(activeAlarmsSection, 0, 3, 3, 1);
        grid.add(tableView, 0,0,8,1);
        grid.add(numExpertsButton, 0,1);
        grid.add(expertsOnDutyButton, 1,1);
        grid.add(expertOnPageButton, 2,1);
        grid.add(editScheduleButton, 0,2);  
        grid.add(saveButton, 1,2,2,1);
        grid.add(periodSection, 0,4,3,1);

        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateActiveAlarms(powerSupplyCheck.isSelected(), tankOverflowCheck.isSelected(), co2DetectedCheck.isSelected(), biologicalAttackCheck.isSelected());

        powerSupplyCheck.setOnAction(event ->
                updateActiveAlarms(powerSupplyCheck.isSelected(), tankOverflowCheck.isSelected(), co2DetectedCheck.isSelected(), biologicalAttackCheck.isSelected()));
        tankOverflowCheck.setOnAction(event ->
                updateActiveAlarms(powerSupplyCheck.isSelected(), tankOverflowCheck.isSelected(), co2DetectedCheck.isSelected(), biologicalAttackCheck.isSelected()));
        co2DetectedCheck.setOnAction(event ->
                updateActiveAlarms(powerSupplyCheck.isSelected(), tankOverflowCheck.isSelected(), co2DetectedCheck.isSelected(), biologicalAttackCheck.isSelected()));
        biologicalAttackCheck.setOnAction(event ->
                updateActiveAlarms(powerSupplyCheck.isSelected(), tankOverflowCheck.isSelected(), co2DetectedCheck.isSelected(), biologicalAttackCheck.isSelected()));
    }

    private void updateActiveAlarms(boolean powerSupply, boolean tankOverflow, boolean co2Detected, boolean biologicalAttack){
        activeAlarms.clear();
        if(powerSupply) activeAlarms.add("Power supply missing");
        if(tankOverflow) activeAlarms.add("Tank overflow");
        if(co2Detected) activeAlarms.add("CO2 detected");
        if(biologicalAttack) activeAlarms.add("Biological attack");
        System.out.println("Active Alarms:" + activeAlarms);
    }
    
    //Set up table
    private void setupTable(){
        tableView = new TableView<>();
        TableColumn<ObservableList<String>, String> periodColumn = new TableColumn<>("Period");
        periodColumn.setMinWidth(50);
        periodColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(0)));
        tableView.getColumns().add(periodColumn);

        for(int i = 0; i < expertNames.size(); i++) {
            final int colIndex = i + 1;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(expertNames.get(i));
            column.setMinWidth(25);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
            column.setCellFactory(param -> new javafx.scene.control.TableCell<ObservableList<String>, String>(){
                TextField textField = new TextField();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        textField.setText(item);
                        textField.setEditable(isEditable);
                        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if(!newValue && isEditable) {
                                ObservableList<String> row = getTableView().getItems().get(getIndex());
                                row.set(colIndex, textField.getText());
                            }
                        });
                        setGraphic(textField);
                    } else {
                        setGraphic(null);
                    }
                }
            });
            tableView.getColumns().add(column);
        }
        populateTable();
    }

    //Schedule data to populate on the table
    private void populateTable(){
        tableView.getItems().clear();
        List<String> periods = new ArrayList<>(schedule.keySet());

        for(String period : periods){
            List<String> row = new ArrayList<>();
            row.add(period);
            for(String expert : expertNames) {
                row.add(schedule.get(period).contains(expert) ? "OK" : "");
            }
            tableView.getItems().add(FXCollections.observableArrayList(row));
        }
        System.out.println("Populated Table Items: " + tableView.getItems());
    }

    //Toggle edit for table
    private void toggleEditSchedule(){
        isEditable = !isEditable;
        saveButton.setDisable(!isEditable); //To enable, disable save button
        tableView.refresh();
    }

    //Add Period to Schedule
    private void addPeriod(String periodName) {
        if (periodName != null && !periodName.isEmpty() && !schedule.containsKey(periodName)){
            schedule.put(periodName, new HashSet<>());
            populateTable();
            showAlert("Success", "Period Added", "Period" + periodName + "has been added" );
            System.out.println("Schedule After Adding Period:" + schedule);
        } else {
            showAlert("Error", "Invalid Period Name", "Enter valid period name");
        }
    }

    //To Remove period in Schedule
    private void removePeriod(String periodName){
        if (periodName != null && !periodName.isEmpty() && schedule.containsKey(periodName)){
            schedule.remove(periodName);
            populateTable();
            showAlert("Success", "Period Added", "Period" + periodName + "has been removed" );
            System.out.println("Schedule After Removing Period:" + schedule);
        } else {
            showAlert("Error", "Invalid Period Name", "Enter valid period name");
        }
    }

    //Expert on Duty function
    private void showExpertsOnDuty(){
        showTextInputDialog("Experts on Duty", "Enter Expert Number (1-8):", "Expert Number:", "1", input ->{
            try {
                int expertNum = Integer.parseInt(input);
                if(expertNum < 1 || expertNum > expertNames.size()){
                    showAlert("Error", "Invalid Expert Number", "Please enter a number between 1 and " + expertNames.size());
                    return;
                }

                String expertName = expertNames.get(expertNum - 1); 
                List<String> availablePeriods = new ArrayList<>();
                for(Map.Entry<String, Set<String>> entry : schedule.entrySet()) {
                    if(entry.getValue().contains(expertName)) {
                        availablePeriods.add(entry.getKey());
                    }
                }

                showAlert("Experts on Duty", expertName, "Available Periods: " + availablePeriods);
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid Input", "Please enter number between 1 and " + expertNames.size());
            }

        });
    }

    private void saveSchedule(){
        schedule.clear();
        for(int rowIndex = 0; rowIndex < tableView.getItems().size(); rowIndex++) {
            ObservableList<String> row = tableView.getItems().get(rowIndex);
            String period = row.get(0);
            Set<String> expertsOnDuty = new HashSet<>();
            for(int colIndex = 1; colIndex < row.size(); colIndex++){
                String cellValue = row.get(colIndex);
                if("OK".equals(cellValue)) {
                    expertsOnDuty.add(expertNames.get(colIndex - 1));
                }
            }
            schedule.put(period,expertsOnDuty);
        }
        showAlert("Success", "Schedule Saved", "Schedule saved");
        isEditable = false;
        populateTable();
        tableView.refresh();
        System.out.println("Schedule after save:" + schedule);
    }

    //Number of Experts function
    private int getNumberOfExperts(String period){
        if(schedule.containsKey(period)) {
            Set<String> experts = schedule.get(period);
            if(experts != null) {
                return experts.size();
            }else {
                System.err.println("Warning: Experts set for period is null");
                return 0;
            }
        }else{
            System.err.println("Warning: Period not found");
            return 0;
        }
    }

    //Expert on Page function
    private void showExpertOnPage(){
        Stage popupStage = new Stage();
        popupStage.setTitle("Select Period, Alarm and ExpertID");

        Label periodLabel = new Label("Select Period:");
        Label alarmLabel = new Label("Select Alarm");
        Label eidLabel = new Label("Enter Expert ID");
        TextField eidField = new TextField();

        ObservableList<String> periodDisplayNames = FXCollections.observableArrayList(schedule.keySet());
        ChoiceBox<String> periodChoiceBox = new ChoiceBox<>(periodDisplayNames);

        ObservableList<String> alarmDisplayNames = FXCollections.observableArrayList(activeAlarms);
        ChoiceBox<String> alarmChoiceBox = new ChoiceBox<>(alarmDisplayNames);

        Button okButton = new Button("OK");

        okButton.setOnAction(event -> {
            String selectedPeriod = periodChoiceBox.getValue();
            String selectedAlarm = alarmChoiceBox.getValue();
            String eidText = eidField.getText();

            int enteredEid = 0;

            try{
                enteredEid = Integer.parseInt(eidText);
            } catch (NumberFormatException e){
                showAlert("Error", "Invalid Expert Id", "Please enter a valid number");
                return;
            }

            if(selectedPeriod == null || selectedPeriod.equals("Select Period")){
                showAlert("Error", "Period Selection", "Select a period");
            }else if(selectedAlarm == null || selectedAlarm.equals("Select Alarm")){
                showAlert("Error", "Alarm Selection", "Select an Alarm");
            }else{
                displayExpert(selectedPeriod, selectedAlarm, enteredEid);
                
                boolean preConditionResult = pre_ExpertToPage(selectedAlarm, selectedPeriod);
                System.out.println("Precondition Result:" + preConditionResult);
                
                boolean postConditionResult = post_ExpertToPage(selectedAlarm, selectedPeriod, enteredEid);
                System.out.println("Postcondition Result:" + postConditionResult);
                
                popupStage.close();

                int numberOfExperts = getNumberOfExperts(selectedPeriod);
                System.out.println("Number of Experts" + numberOfExperts);
            }
        });

        VBox vbox = new VBox(10, periodLabel, periodChoiceBox, alarmLabel, alarmChoiceBox, eidLabel, eidField, okButton);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setPadding(new javafx.geometry.Insets(10));
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 350, 250);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void displayExpert(String period, String alarm, int enteredEid){
        if(!alarmQualifications.containsKey(alarm)){
            showAlert("Error","Invalid Alarm","Alarm is invalid");
            return;
        }

        String requiredQualification = alarmQualifications.get(alarm);
        List<String> qualifiedExperts = new ArrayList<>();

        for(int rowIndex = 0; rowIndex < tableView.getItems().size(); rowIndex++){
            ObservableList<String> row = tableView.getItems().get(rowIndex);
            if(row.get(0).equals(period)) {
                for(int colIndex = 1; colIndex < row.size(); colIndex++){
                    String cellValue = row.get(colIndex);
                    if("OK".equals(cellValue)){
                        String expert = expertNames.get(colIndex - 1);
                        if(expertQualifications.containsKey(expert) && expertQualifications.get(expert).contains(requiredQualification)){
                            qualifiedExperts.add(expert);
                        }
                    }
                }
                break;
            }
        }

        boolean eidQualified = false;
        for(String expertName : qualifiedExperts){
            if(expertIDs.get(expertName) == enteredEid){
                eidQualified = true;
                break;
            }
        }

        if(!eidQualified){
            showAlert("Expert on Page", "No Qualified expert", "No Qualified expert found for the selected period, alarm, eid");
        } else{
            String expertsList = String.join(",",qualifiedExperts);
            showAlert("Expert on Page", "Qualified Experts:", expertsList);
        }
            
    }

    //Pre-condition
    private boolean pre_ExpertToPage(String alarmName, String period){
        if(!activeAlarms.contains(alarmName)){
            return false;
        }
        if(!schedule.containsKey(period)){
            return false;
        }
        return true;
    }

    //Post-condition
    private boolean post_ExpertToPage(String alarmName, String period, int enteredEid){
        String requiredQualification = alarmQualifications.get(alarmName);
        Set<String> expertsOnDuty = schedule.get(period);
        for(String expertName : expertsOnDuty){
            if(expertIDs.get(expertName) == enteredEid && expertQualifications.get(expertName).contains(requiredQualification)){
                return true;
            }
        }
        return false;
    }

    private void showTextInputDialog(String title, String header, String content, String example, TextInputDialogCallback callback){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content+"(eg.,"+example+")");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(callback::onInputReceived);
    }

    //For Alerts
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //to handle input
    @FunctionalInterface
    interface TextInputDialogCallback{
        void onInputReceived(String input);
    }

    //main method
    public static void main(String[] args){
        launch(args);
    }
}
    



