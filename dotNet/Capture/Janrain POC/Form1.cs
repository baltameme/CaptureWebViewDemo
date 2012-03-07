using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.Security.Permissions;
using System.Windows.Forms;
using System.Net;
using System.IO;

namespace WindowsFormsApplication1
{
    [PermissionSet(SecurityAction.Demand, Name = "FullTrust")]
    [System.Runtime.InteropServices.ComVisibleAttribute(true)]
    public partial class Form1 : Form
    {
        String captureServer = "https://webview-poc.dev.janraincapture.com";
        String redirectUri = "http://10.0.0.96/~chad/intelCapture.html";
        String clientId = "zc7tx83fqy68mper69mxbt5dfvd7c2jh";
        String responseType = "token";
        public Form1()
        {
            InitializeComponent();

            // Navigate to a web page that has the custome widget embedded.
            webBrowser1.Navigate(new System.Uri(captureServer + "/oauth/signin_mobile?redirect_uri=" + redirectUri + "&client_id=" + clientId + "&response_type=" + responseType));
        }

        // Can use window.external.setProfileName(profileName) to call this method from JavaScript.
        public void setProfileName(string profileName)
        {
            Console.WriteLine(profileName); 
        }

        public void getProfile(string token)
        {
            string url = "https://webview-poc.dev.janraincapture.com/entity?access_token=" + token;
            WebRequest request = WebRequest.Create(url);

            request.ContentType = "text/html";
            request.Method = "GET";

            HttpWebResponse response = (HttpWebResponse)request.GetResponse();
            
            // Get the stream containing content returned by the server.
            Stream dataStream = response.GetResponseStream();
            
            // Open the stream using a StreamReader for easy access.
            StreamReader reader = new StreamReader(dataStream);

            // Read the content.
            string responseFromServer = reader.ReadToEnd();

            // Cleanup the streams and the response.
            reader.Close();
            dataStream.Close();
            response.Close();

            // Resize a few items... 
            Form1.ActiveForm.Height = 500;
            Form1.ActiveForm.Width = 940;
            loading.Height = 444;
            loading.Width = 889;
            loading.Text = responseFromServer;
            loading.Show();

            // Hide the webBrowser
            webBrowser1.Hide();
        }

        // Can use window.external.setStatus(status) to call this method from JavaScript.
        public void setStatus(string text)
        {
            statusLabel.Text = text;
        }

        // This is called from the janrainWidgetOnload function.
        public void hideLoadingLabel()
        {
            loading.Hide();
        }

        public void setHeight(int height)
        {
            // Adding a little bit of padding as these numbers are not ideal.
            if (height > 0) this.Height = height + 30;
        }

        public void setWidth(int width)
        {
            // Adding padding...
            if (width > 0) this.Width = width + 30;
        }

        // Allow JavaScript to make calls to the app.
        public void loadHandler(object sender, EventArgs e)
        {
            webBrowser1.ObjectForScripting = this;
        }

    }
}
