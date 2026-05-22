import React from 'react';
import './Footer.scss';

const Footer: React.FC = () => {
    return (
        <footer className="site-footer">
            <div className="site-footer__inner">
                <div className="site-footer__brand">
                    <h3>Marketplace</h3>
                    <p>A modern peer-to-peer marketplace built with React, TypeScript and Spring Boot.</p>
                </div>

                <div className="site-footer__links">
                    <a
                        href="https://www.linkedin.com/in/yaroslav-ivanchenko-a42a592b0/"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        LinkedIn
                    </a>
                    <a
                        href="https://github.com/shagid76"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        GitHub
                    </a>
                </div>
            </div>

            <p className="site-footer__copy">
                &copy; {new Date().getFullYear()} Yaroslav Ivanchenko. Crafted as a portfolio project.
            </p>
        </footer>
    );
};

export default Footer;
