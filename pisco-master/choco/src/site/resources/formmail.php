<!-- CPRU_CHOCO--><?php
/* Récupération des valeurs des champs du formulaire */
if (get_magic_quotes_gpc())
{
    $nom = stripslashes($_POST['nom']);
    $expediteur = stripslashes($_POST['email']);
    $profession = stripslashes($_POST['prof']);
    $message = stripslashes($_POST['comments']);
}
else
{
    $nom = $_POST['nom'];
    $expediteur = $_POST['email'];
    $profession = $_POST['prof'];
    $message = $_POST['comments'];
}
$sujet="[CHOCO][Utilisateur]".$nom;
$message = " Nom:".$nom."\n Email: ".$expediteur."\n Profession: ".$profession."\n Message:".$message."\n IP:".$_SERVER["REMOTE_ADDR"];


/* Destinataire (votre adresse e-mail) */
$to = 'choco@emn.fr';

/* Construction du message */
/* En-têtes de l'e-mail */
$headers = 'From: '.$nom.' <'.$expediteur.'>'."\r\n\r\n";

/* Envoi de l'e-mail */
if($message!=" Nom:\n Email: \n Profession: \n Message:"){
   mail($to, $sujet, $message, $headers);
}
header("Location: /choco-about-you.html");
?>
