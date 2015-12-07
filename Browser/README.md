# Unique Password Generator

Generate unique passwords for each site using just one master password
Create 64 characters long password from any memorable master password (the only one you have to remember) that's different for every site you use it on.

Click the *** icon, set the site name and master password, highlight the right password box with 'Next', then choose 'Set' - your secure scrambled site-specific password will be automatically added to the password box.

You can reveal the password or hide it again by clicking 'Show' or 'Hide'.

Passwords are created at the SHA256 hash of the master password and the site domain.

- The hash is one-way; it ensures that your master password can't be reconstructed from the password you send to a site.
- The site domain within the hash means a different password will be sent to every site; if the site gets hacked, all of your other passwords (and your master password) are still completely safe.

If you want to use a password but aren't using Chrome (such as from your phone), you can use the web application at [https://dave-cassettari.github.io/unique-password/](https://dave-cassettari.github.io/unique-password/) to create the password, then copy & paste it.