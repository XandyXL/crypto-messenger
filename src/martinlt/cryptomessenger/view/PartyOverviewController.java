package martinlt.cryptomessenger.view;

import java.util.Base64;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import martinlt.cryptomessenger.MainApp;
import martinlt.cryptomessenger.model.Party;

public class PartyOverviewController
{
   @FXML
   private ComboBox<Party> partyComboBox;

   @FXML
   private Label publicKeyLabel;

   @FXML
   private Label outputLabel;

   @FXML
   private TextArea messageLabel;

   // Reference to the main application.
   private MainApp mainApp;

   private static final String NEWLINE = "\n";

   /**
    * The constructor. The constructor is called before the initialize() method.
    */
   public PartyOverviewController()
   {
   }

   /**
    * Initializes the controller class. This method is automatically called
    * after the fxml file has been loaded.
    */
   @FXML
   private void initialize()
   {
      // Clear party details.
      showPartyDetails(null);
   }

   /**
    * Is called by the main application to give a reference back to itself.
    *
    * @param mainApp
    */
   public void setMainApp(MainApp mainApp)
   {
      this.mainApp = mainApp;

      ObservableList<Party> partyData = mainApp.getPartyData();

      if (partyData == null)
         System.out.println("ERROR: no parties found");
      else {

         // Add observable list data to the table
         partyComboBox.setItems(this.mainApp.getPartyData());
      }
   }

   /**
    * Fills all text fields to show details about the party. If the specified
    * party is null, all text fields are cleared.
    *
    * @param party
    *           the party or null
    */
   private void showPartyDetails(Party party)
   {
      if (party != null) {
         // Fill the labels with info from the party object.
         publicKeyLabel.setText(party.getPublicKey());

      } else {
         // Party is null, remove all the text.
         publicKeyLabel.setText("");

      }
   }

   @FXML
   private void handleComboBoxAction()
   {
      showPartyDetails(partyComboBox.getSelectionModel().getSelectedItem());
   }

   @FXML
   private void handleCopyToClipboard()
   {
      final Clipboard clipboard = Clipboard.getSystemClipboard();
      final ClipboardContent content = new ClipboardContent();
      content.putString(wordWrap(outputLabel.getText(), 80));
      clipboard.setContent(content);
   }

   @FXML
   private void handleShowMyPublicKey()
   {
      outputLabel.setText(mainApp.getPublicKey());
   }

   @FXML
   private void handleEncrypt()
   {
      Party party = partyComboBox.getSelectionModel().getSelectedItem();
      if (party != null) {
         String message = messageLabel.getText();
         // System.out.println("DEBUG: " + message);
         if (message != null) {
            try {
               mainApp.encryptMessage(message, party.getIdentifier());

               outputLabel.setText(mainApp.getCipherText());
            } catch (Exception ex) {
               Alert alert = new Alert(AlertType.ERROR);
               alert.initOwner(mainApp.getPrimaryStage());
               alert.setTitle("An error occurred");
               alert.setHeaderText("Encryption error");
               alert.setContentText("The text you provided could not be encrypted.");

               alert.showAndWait();
            }

         }
      }
   }

   @FXML
   private void handleDecrypt()
   {
      Party party = partyComboBox.getSelectionModel().getSelectedItem();
      if (party != null) {
         String message = messageLabel.getText().replace(NEWLINE, "");

         if (message != null) {
            try {
               mainApp.receiveAndDecryptMessage(Base64.getDecoder().decode(message));

               outputLabel.setText(mainApp.getPlainText());
            } catch (Exception ex) {
               Alert alert = new Alert(AlertType.ERROR);
               alert.initOwner(mainApp.getPrimaryStage());
               alert.setTitle("An error occurred");
               alert.setHeaderText("Decryption error");
               alert.setContentText("The text you provided could not be decrypted.");

               alert.showAndWait();
            }

         }
      }
   }

   /**
    * Called when the user clicks on the delete button.
    */
   @FXML
   private void handleDeleteParty()
   {
      Party party = partyComboBox.getSelectionModel().getSelectedItem();
      if (party != null)
         mainApp.remove(party.getIdentifier());
   }

   /**
    * Performs word wrapping. Returns the input string with long lines of text
    * cut (between words) for readability.
    *
    * @param in
    *           text to be word-wrapped
    * @param length
    *           number of characters in a line
    */
   public static String wordWrap(String in, int length)
   {
      if (in == null)
         return "";

      // :: Trim
      while (in.length() > 0 && (in.charAt(0) == '\t' || in.charAt(0) == ' '))
         in = in.substring(1);

      // :: If Small Enough Already, Return Original
      if (in.length() < length)
         return in;

      // :: If Next length Contains Newline, Split There
      if (in.substring(0, length).contains(NEWLINE))
         return in.substring(0, in.indexOf(NEWLINE)).trim() + NEWLINE
               + wordWrap(in.substring(in.indexOf("\n") + 1), length);

      // :: Otherwise, Split Along Nearest Previous Space/Tab/Dash
      int spaceIndex = Math.max(Math.max(in.lastIndexOf(" ", length), in.lastIndexOf("\t", length)),
            in.lastIndexOf("-", length));

      // :: If No Nearest Space, Split At length
      if (spaceIndex == -1)
         spaceIndex = length;

      // :: Split
      return in.substring(0, spaceIndex).trim() + NEWLINE
            + wordWrap(in.substring(spaceIndex), length);
   }

   /**
    * Called when the user clicks the new button. Opens a dialog to edit details
    * for a new party.
    */
   @FXML
   private void handleNewParty()
   {
      Party tempParty = new Party();
      boolean okClicked = mainApp.showPartyNewDialog(tempParty);
      if (okClicked) {
         try {
            mainApp.receivePublicKeyFrom(tempParty.getIdentifier(), tempParty.getPublicKey());

         } catch (martinlt.cryptomessenger.exception.SecurityException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("An error has occurred");
            alert.setHeaderText("Invalid public key");
            alert.setContentText("The public key you have provided does not appear to be valid.");

            alert.showAndWait();
         }

      }
   }

   /**
    * Called when the user clicks the edit button. Opens a dialog to edit
    * details for the selected party.
    */
   @FXML
   private void handleEditParty()
   {
      Party selectedParty = partyComboBox.getSelectionModel().getSelectedItem();
      if (selectedParty != null) {
         String currentIdentifier = new String(selectedParty.getIdentifier());
         Party savedParty = new Party(new String(selectedParty.getIdentifier()), new String(selectedParty.getPublicKey()));
         boolean okClicked = mainApp.showPartyEditDialog(selectedParty);
         if (okClicked) {
            try {
               mainApp.receivePublicKeyFrom(selectedParty.getIdentifier(), selectedParty.getPublicKey());
               if (selectedParty.getIdentifier().compareTo(currentIdentifier) != 0)
                  mainApp.remove(currentIdentifier);
            } catch (martinlt.cryptomessenger.exception.SecurityException e) {
               Alert alert = new Alert(AlertType.ERROR);
               alert.initOwner(mainApp.getPrimaryStage());
               alert.setTitle("An error has occurred");
               alert.setHeaderText("Invalid public key");
               alert.setContentText("The public key you have provided does not appear to be valid.");

               alert.showAndWait();

               selectedParty.setIdentifier(savedParty.getIdentifier());
               selectedParty.setPublicKey(savedParty.getPublicKey());
            }

            showPartyDetails(selectedParty);
         }

      } else {
         // Nothing selected.
         Alert alert = new Alert(AlertType.WARNING);
         alert.initOwner(mainApp.getPrimaryStage());
         alert.setTitle("No Selection");
         alert.setHeaderText("No Party Selected");
         alert.setContentText("Please select a party from the list.");

         alert.showAndWait();
      }
   }

}
