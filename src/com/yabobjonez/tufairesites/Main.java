package com.yabobjonez.tufairesites;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;

import dev.anarchy.ace.AceEditor;
import dev.anarchy.ace.Modes;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application { //TODO theming, builder
	public static void main(String[] args) { launch(args); }
	private File baseDir = null;
	private File currFile = null;
	@Override
	public void start(Stage stage) throws Exception {
		TextInputDialog dlgName = new TextInputDialog();
		dlgName.setHeaderText("Введите имя файла с расширением (.html/.css/.js).");
		dlgName.setContentText("Имя файла:");
		dlgName.setOnShowing(e -> dlgName.getEditor().clear());
		TextInputDialog dlgNameFolder = new TextInputDialog();
		dlgNameFolder.setHeaderText("Введите имя папки.");
		dlgNameFolder.setContentText("Имя папки:");
		dlgNameFolder.setOnShowing(e -> dlgNameFolder.getEditor().clear());
		DirectoryChooser dlgDir = new DirectoryChooser();
		dlgDir.setTitle("Выберите папку");
		Alert dlgDel = new Alert(AlertType.CONFIRMATION, "Вы действительно хотите удалить выбранное?", ButtonType.YES, ButtonType.NO);
		dlgDel.setHeaderText("Удаление.");
		TreeView<String> explorer = new TreeView<>();
		WebView view = new WebView();
		AceEditor editor = new AceEditor();
		Button btnOpen = new Button(); btnOpen.setGraphic(getIcon("open.png")); btnOpen.setOnAction(e -> {
			this.baseDir = dlgDir.showDialog(stage);
			if(this.baseDir!=null) explorer.setRoot(this.intlBuildTree(this.baseDir));
		});
		Button btnRefresh = new Button(); btnRefresh.setGraphic(getIcon("refresh.png")); btnRefresh.setOnAction(e -> {
			if(this.baseDir!=null) explorer.setRoot(this.intlBuildTree(this.baseDir));
		});
		Button btnSave = new Button(); btnSave.setGraphic(getIcon("save.png"));btnSave.setOnAction(e -> {
			if(this.baseDir!=null && this.currFile!=null) try {
				Files.writeString(this.currFile.toPath(), editor.getText(),
					Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch(IOException exc){ System.err.println("Ошибка R/W!"); }
		});
		Button btnDelete = new Button(); btnDelete.setGraphic(getIcon("delete.png")); btnDelete.setOnAction(e -> { if(this.baseDir!=null){
			if(!explorer.getSelectionModel().isEmpty() && explorer.getSelectionModel().getSelectedItem().getValue()
			.equals(this.baseDir.getName())){
				String file = "";
				for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
					selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
				try { if(dlgDel.showAndWait().get() == ButtonType.YES){
					Files.walk(new File(this.baseDir, file).toPath()).sorted(Comparator.reverseOrder())
						.map(Path::toFile).forEach(File::delete);
					explorer.setRoot(this.intlBuildTree(this.baseDir));
				}} catch(IOException exc) { System.err.println("Ошибка DW!"); }
			}
		}});
		Button btnNewF = new Button(); btnNewF.setGraphic(getIcon("newfolder.png")); btnNewF.setOnAction(e -> { if(this.baseDir!=null){
			if(explorer.getSelectionModel().isEmpty()) return;
			String file = "";
			for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
				selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
			File f = new File(this.baseDir, file); if(f.isFile()) return;
			dlgNameFolder.showAndWait().ifPresent(name -> {
				if(!new File(f, name).mkdirs()){ System.err.println("Ошибка DirR/W!"); }
				explorer.setRoot(this.intlBuildTree(this.baseDir));
			});
		}});
		Button btnNewf = new Button(); btnNewf.setGraphic(getIcon("newfile.png")); btnNewf.setOnAction(e -> { if(this.baseDir!=null){
			if(explorer.getSelectionModel().isEmpty()) return;
			String file = "";
			for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
				selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
			File f2 = new File(this.baseDir, file); if(f2.isFile()) return;
			dlgName.showAndWait().ifPresent(name -> { try {
				File f = new File(f2, name); f.createNewFile();
				if(f.getName().endsWith(".html")){ Files.writeString(f.toPath(),
					"<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<meta charset=\"utf-8\">\n\t\t<title></title>\n\t</head>\n\t<body>\n"
					+"\t\t\n\t</body>\n</html>",
					Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
					editor.setMode(Modes.HTML); }
				else if(f.getName().endsWith(".css")) editor.setMode(Modes.CSS);
				else if(f.getName().endsWith(".js")) editor.setMode(Modes.JavaScript);
				editor.setText(Files.readString(f.toPath()));
				explorer.setRoot(this.intlBuildTree(this.baseDir));
			} catch(IOException exc){ System.err.println("Ошибка R/W!"); } });
		}}); Button btnRun = new Button(); btnRun.setGraphic(getIcon("run.png")); btnRun.setOnAction(e -> {
			if(this.baseDir!=null && this.currFile!=null){ view.getEngine().load(this.currFile.toURI().toString()); view.getEngine().reload(); }
		}); Alert dlgAbout = new Alert(AlertType.INFORMATION, "", ButtonType.CLOSE);
		dlgAbout.setTitle("О программе");
		dlgAbout.setHeaderText("TuFaireSites Editor");
		dlgAbout.setContentText("Версия: 1.2.0-rc\nАвтор: Михайло Стецюк (ya_Bob_Jonez)\nЛицензия: НЕ ДЛЯ РАСПРОСТРАНЕНИЯ\n\n"
			+"Использовано:\nOpenJDK 11.0 [GPLv2+CE]\nOpenJFX 17.0.2 [GPLv2+CE]\nAceFX (TFSEdit) [Apache2.0]\nhttps://icons8.com");
		Button btnAbout = new Button(); btnAbout.setGraphic(getIcon("about.png")); btnAbout.setOnAction(e -> dlgAbout.show());
		Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
		ToolBar toolbar = new ToolBar(
			btnOpen, btnRefresh, new Separator(Orientation.VERTICAL),
			btnNewF, btnNewf, btnDelete, btnSave, new Separator(Orientation.VERTICAL),
			btnRun, spacer,
			btnAbout
		); explorer.setOnMouseClicked(e -> {
			if(e.getClickCount()==2){
				if(explorer.getSelectionModel().isEmpty()) return;
				String file = "";
				for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
					selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
				File f = new File(this.baseDir, file);
				if(f.isFile()){ try {
					boolean isSupported = false; for(String ln : new String[]{".html", ".css", ".js"}) if(f.getName().endsWith(ln)) isSupported = true;
					if(!isSupported) return;
					if(this.currFile!=null) Files.writeString(this.currFile.toPath(), editor.getText(),
						Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
					this.currFile = f;
					if(currFile.getName().endsWith(".html")) editor.setMode(Modes.HTML);
					else if(currFile.getName().endsWith(".css")) editor.setMode(Modes.CSS);
					else if(currFile.getName().endsWith(".js")) editor.setMode(Modes.JavaScript);
					editor.setText(Files.readString(f.toPath()));
					} catch(IOException exc){ System.err.println("Ошибка R/W!"); }
				} else if(f.isDirectory() && !explorer.getSelectionModel().getSelectedItem().getChildren().isEmpty())
					explorer.getSelectionModel().getSelectedItem().setExpanded(!explorer.getSelectionModel().getSelectedItem().isExpanded());
			} else if(e.getButton() == MouseButton.SECONDARY) explorer.getSelectionModel().clearSelection();
		});
		SplitPane main = new SplitPane(explorer, view, editor);
		main.setDividerPositions(0.2, 0.6);
		VBox.setVgrow(main, Priority.ALWAYS);
		VBox root = new VBox(toolbar, main);
		Scene scene = new Scene(root, 640, 480);
		scene.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.F5) btnRun.fireEvent(new ActionEvent());
			else if(e.getCode() == KeyCode.F6){ btnSave.fireEvent(new ActionEvent()); btnRun.fireEvent(new ActionEvent()); }
			else if(e.getCode() == KeyCode.F1) this.builderText();
			else if(e.getCode() == KeyCode.F2) this.builderTables();
		}); stage.setTitle("TFSEdit by ya_Bob_Jonez");
		stage.setScene(scene);
		stage.show();
	} private TreeItem<String> intlBuildTree(File dir){
		TreeItem<String> treeItem = new TreeItem<>(dir.getName());
		for(File f : dir.listFiles()){
			if(f.isDirectory()) treeItem.getChildren().add(this.intlBuildTree(f));
			else treeItem.getChildren().add(new TreeItem<String>(f.getName()));
		} return treeItem;
	} private ImageView getIcon(String rel){ ImageView t = new ImageView(getClass().getResource("img/"+rel).toExternalForm());
		t.setFitWidth(30); t.setPreserveRatio(true); return t; }
	public void builderText(){
		Stage bwText = new Stage(StageStyle.UTILITY);
		ObservableList<String> availFonts = FXCollections.observableArrayList(
			"Arial", "Arial Black", "Verdana", "Helvetica", "Tahoma", "Trebuchet MS", "Times New Roman", "Georgia", "Garamond", "Courier New",
			"Brush Script MT", "Palatino", "Papyrus", "Impact", "Comic Sans MS"
		); FXCollections.sort(availFonts); ComboBox<String> fontFamily = new ComboBox<>(availFonts);
		fontFamily.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(fontFamily, Priority.ALWAYS);
		fontFamily.setPromptText("Шрифт");
		fontFamily.setEditable(true);
		ComboBox<String> fontGeneric = new ComboBox<>(FXCollections.observableArrayList("Serif", "Sans-serif", "Monospace", "Cursive", "Fantasy"));
		fontGeneric.setPromptText("Вид");
		fontGeneric.setMaxWidth(120);
		fontGeneric.setEditable(true);
		Spinner<Double> fontSize = new Spinner<>(0, 160.0, 0, 0.5);
		fontSize.setMaxWidth(85);
		fontSize.setEditable(true);
		ColorPicker fontColor = new ColorPicker(Color.BLACK);
		fontColor.setStyle("-fx-color-label-visible:false;");
		HBox font = new HBox(7, fontFamily, fontGeneric, fontSize, fontColor); font.setPadding(new Insets(7));
		ToggleButton bold = new ToggleButton(); bold.getStyleClass().add("html-editor-bold");
		ToggleButton italic = new ToggleButton(); italic.getStyleClass().add("html-editor-italic");
		ToggleButton strike = new ToggleButton(); strike.getStyleClass().add("html-editor-strike");
		ToggleButton under = new ToggleButton(); under.getStyleClass().add("html-editor-underline");
		ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Абзац", "Заголовок XL", "Заголовок L", "Заголовок M", "Заголовок S",
			"Заголовок XS", "Заголовок XXS")); type.setValue("Абзац");
		type.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(type, Priority.ALWAYS);
		ColorPicker bgColor = new ColorPicker(Color.TRANSPARENT);
		bgColor.setStyle("-fx-color-label-visible:false;");
		HBox definition = new HBox(7, bold, italic, strike, under, type, bgColor); definition.setPadding(new Insets(7));
		WebView preview = new WebView(); preview.getEngine().loadContent("<html><body></body></html>");
		TextArea codeT = new TextArea(); codeT.setWrapText(true);
		Button btnGenerate = new Button("Сгенерировать код"); btnGenerate.setOnAction(e -> {
			String code = "<"; String cType = type.getValue();
			String tag = cType.equals("Абзац")?"p":cType.equals("Заголовок XL")?"h1":cType.equals("Заголовок L")?"h2":cType.equals("Заголовок M")?"h3":
				cType.equals("Заголовок S")?"h4":cType.equals("Заголовок XS")?"h5":"h6";
			code += tag + " style=\""; if(!fontFamily.getValue().isBlank() && fontSize.getValue()!=0.0){ code += "font: "+(italic.isSelected()?
				"italic ":"")+(bold.isSelected()?"bold ":"")+fontSize.getValue().toString()+"pt '"+fontFamily.getValue()+"'"+(!fontGeneric.getValue()
				.isBlank()?", "+fontGeneric.getValue().toLowerCase():"")+"; "; } else { code+=!fontFamily.getValue().isBlank()?"font-family: '"+
				fontFamily.getValue()+(!fontGeneric.getValue().isBlank()?"', "+fontGeneric.getValue().toLowerCase():"'")+"; ":""; code+=fontSize
				.getValue()!=0.0?"font-size: "+fontSize.getValue()+"pt; ":""; code+=bold.isSelected()?"font-weight: bold; ":""; code+=italic
				.isSelected()?"font-style: italic; ":""; } if(strike.isSelected() || under.isSelected()){ code+="text-decoration:"+(strike.isSelected()?
				" line-through":"")+(under.isSelected()?" underline":"")+"; "; } if(!fontColor.getValue().equals(Color.BLACK))code+="color: #"+String
				.format("%02X%02X%02X",(int)(fontColor.getValue().getRed()*255),(int)(fontColor.getValue().getGreen()*255),(int)(fontColor.getValue()
				.getBlue()*255))+"; "; if(!bgColor.getValue().equals(Color.WHITE))code+="background: #"+String.format("%02X%02X%02X",(int)(bgColor
				.getValue().getRed()*255),(int)(bgColor.getValue().getGreen()*255),(int)(bgColor.getValue().getBlue()*255))+"; ";
			code += "\">"; codeT.setText(code+"</"+tag+">"); preview.getEngine().loadContent("<html><body>"+code+"Sample<"+tag+"></body></html>");
		}); btnGenerate.setMaxWidth(Double.MAX_VALUE);
		SplitPane creation = new SplitPane(preview, codeT); creation.setPadding(new Insets(7));
		VBox rows = new VBox(font, definition, creation, btnGenerate);
		bwText.setScene(new Scene(rows, 480, 270));
		bwText.setTitle("Генератор текста");
		bwText.show();
	} public void builderTables(){
		Stage bwText = new Stage(StageStyle.UTILITY);
		CheckBox borderCollapse = new CheckBox("Слить границы");
		borderCollapse.setSelected(true);
		ColorPicker borderColor = new ColorPicker(Color.TRANSPARENT); borderColor.setStyle("-fx-color-label-visible:false;");
		Spinner<Integer> borderWidth = new Spinner<>(0, 15, 1, 1);
		borderWidth.setMaxWidth(65);
		ComboBox<String> borderStyle = new ComboBox<>(FXCollections.observableArrayList("Сплошная", "Пунктирная", "Точечная", "Двойная", "Впуклая",
			"Опуклая")); borderStyle.setValue("Сплошная");
		TextField id = new TextField();
		id.setPromptText("ID (Класс)");
		Spinner<Integer> elementCols = new Spinner<>(1, 50, 2);
		elementCols.setMaxWidth(65);
		Spinner<Integer> elementRows = new Spinner<>(0, 50, 3);
		elementRows.setMaxWidth(65);
		HBox border = new HBox(7, elementCols, elementRows, id, new Separator(Orientation.VERTICAL), borderCollapse, borderColor, borderWidth,
			borderStyle); border.setPadding(new Insets(7));
		ColorPicker headerColor = new ColorPicker(Color.TRANSPARENT); headerColor.setStyle("-fx-color-label-visible:false;");
		Spinner<Double> headerPadding = new Spinner<>(0, 30.0, 0, 0.5);
		headerPadding.setMaxWidth(80);
		ColorPicker element1Color = new ColorPicker(Color.TRANSPARENT); element1Color.setStyle("-fx-color-label-visible:false;");
		ColorPicker element2Color = new ColorPicker(Color.TRANSPARENT); element2Color.setStyle("-fx-color-label-visible:false;");
		Spinner<Double> elementPadding = new Spinner<>(0, 30.0, 0, 0.5);
		elementPadding.setMaxWidth(80);
		ColorPicker borderIColor = new ColorPicker(Color.TRANSPARENT); borderIColor.setStyle("-fx-color-label-visible:false;");
		Spinner<Integer> borderIWidth = new Spinner<>(0, 15, 1, 1);
		borderIWidth.setMaxWidth(65);
		ComboBox<String> borderIStyle = new ComboBox<>(FXCollections.observableArrayList("Сплошная", "Пунктирная", "Точечная", "Двойная", "Впуклая",
			"Опуклая")); borderIStyle.setValue("Сплошная");
		HBox table = new HBox(7, headerColor, headerPadding, new Separator(Orientation.VERTICAL), element1Color, element2Color, elementPadding,
			new Separator(Orientation.VERTICAL), borderIColor, borderIWidth, borderIStyle); table.setPadding(new Insets(0, 7, 7, 7));
		WebView preview = new WebView(); preview.getEngine().loadContent("<html><body></body></html>");
		TextArea styleT = new TextArea(); styleT.setWrapText(true);
		TextArea markT = new TextArea(); markT.setWrapText(true);
		VBox codeArea = new VBox(7, styleT, markT);
		SplitPane creation = new SplitPane(preview, codeArea); creation.setPadding(new Insets(7));
		Button btnGenerate = new Button("Сгенерировать код"); btnGenerate.setOnAction(e -> {
			String stV = borderStyle.getValue(); String stIV = borderIStyle.getValue();
			String st = stV.equals("Сплошная")?"solid":stV.equals("Пунктирная")?"dashed":stV.equals("Точечная")?"dotted":stV.equals("Двойная")?"double":
				stV.equals("Впуклая")?"groove":"ridge"; String stI = stIV.equals("Сплошная")?"solid":stIV.equals("Пунктирная")?"dashed":stIV.equals(
				"Точечная")?"dotted":stIV.equals("Двойная")?"double":stIV.equals("Впуклая")?"groove":"ridge";
			String style = "."+id.getText()+" {\n"; if(borderCollapse.isSelected())style+="\tborder-collapse: collapse;\n"; if(!borderColor.getValue()
				.equals(Color.BLACK)||borderWidth.getValue()!=1||!borderStyle.getValue().equals("Сплошная")){style+="\tborder: "+(borderWidth
				.getValue()!=1?borderWidth.getValue()+"px ":"")+st+(!borderColor.getValue().equals(Color.BLACK)?String.format(" #%02X%02X%02X",(int)
				(borderColor.getValue().getRed()*255),(int)(borderColor.getValue().getGreen()*255),(int)(borderColor.getValue().getBlue()*255)):"")+
				";\n";} style+="}\n."+id.getText()+" th {\n"+(!headerColor.getValue().equals(Color.TRANSPARENT)?"\tbackground: #"+String.format(
				"%02X%02X%02X",(int)(headerColor.getValue().getRed()*255),(int)(headerColor.getValue().getGreen()*255),(int)(headerColor.getValue()
				.getBlue()*255))+";\n":"")+(headerPadding.getValue()!=0.0?"\tpadding: "+headerPadding.getValue()+"px;\n":"")+"}\n."+id.getText()+" td, ."
				+id.getText()+" th {\n"; if(!borderIColor.getValue().equals(Color.BLACK)||borderIWidth.getValue()!=1||!borderIStyle.getValue().equals(
				"Сплошная")){style+="\tborder: "+(borderIWidth.getValue()!=1?borderIWidth.getValue()+" px":"")+stI+(!borderIColor.getValue().equals(
				Color.BLACK)?String.format(" #%02X%02X%02X",(int)(borderIColor.getValue().getRed()*255),(int)(borderIColor.getValue().getGreen()*255),
				(int)(borderIColor.getValue().getBlue()*255)):"")+";\n";} if(elementPadding.getValue()!=0)style+="\tpadding: "+elementPadding.getValue()
				+"px;\n"; style+="}\n"; if(!element1Color.getValue().equals(Color.TRANSPARENT))style+="."+id.getText()+" tr:nth-child(2n){background:#"
				+String.format("%02X%02X%02X",(int)(element1Color.getValue().getRed()*255),(int)(element1Color.getValue().getGreen()*255),(int)(
				element1Color.getValue().getBlue()*255))+";}\n"; if(!element2Color.getValue().equals(Color.TRANSPARENT))style+="."+id.getText()+
				" tr:nth-child(2n+1){background:#"+String.format("%02X%02X%02X",(int)(element2Color.getValue().getRed()*255),(int)(element2Color
				.getValue().getGreen()*255),(int)(element2Color.getValue().getBlue()*255))+";}\n";
			String code = "<table class='"+id.getText()+"'>\n\t<tr>\n";for(int c=1;c<=elementCols.getValue();c++)code+="\t\t<th>H</th>\n";code+=
				"\t</tr>\n"; for(int r=1;r<=elementRows.getValue();r++){code+="\t<tr>\n";for(int c=1;c<=elementCols.getValue();c++)code+="\t\t<td>"+
				"A</td>\n";code+="\t</tr>\n";} code+="</table>\n";
			styleT.setText(style); markT.setText(code); preview.getEngine().loadContent("<html><head><style>\n"+style+"</style></head><body>\n"+
				code+"</body></html>");
		}); btnGenerate.setMaxWidth(Double.MAX_VALUE);
		VBox rows = new VBox(7, border, table, creation, btnGenerate);
		bwText.setScene(new Scene(rows, 745, 400));
		bwText.setTitle("Генератор таблиц");
		bwText.show();
	}
}
